package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.his.charge.dto.PayChannelManualDTO;
import com.his.charge.entity.BizPayChannelBill;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.mapper.BizPayChannelBillMapper;
import com.his.charge.mapper.BizPaymentTxnMapper;
import com.his.charge.service.PayChannelReconciliationService;
import com.his.pay.config.AlipayProperties;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付渠道对账服务实现
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PayChannelReconciliationServiceImpl extends ServiceImpl<BizPayChannelBillMapper, BizPayChannelBill>
        implements PayChannelReconciliationService {

    private final BizPaymentTxnMapper bizPaymentTxnMapper;
    private final AlipayProperties alipayProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importChannelBill(Integer channel, LocalDate billDate) {
        if (!alipayProperties.isMockEnabled() && channel == 2) {
            throw new IllegalStateException(
                    "支付宝对账处于关闭状态（alipay.mock-enabled=false），无法拉取真实账单。" +
                    "如需测试对账，请在 application-local.yml 中配置支付宝凭据并将 mock-enabled 改为 true。"
            );
        }

        List<BizPayChannelBill> bills;
        try {
            if (channel == 1) {
                // TODO: 微信账单下载需要 WxJava 完整实现
                log.warn("[微信支付对账] 拉取微信账单完整实现待补");
                bills = new ArrayList<>();
            } else if (channel == 2) {
                bills = fetchAlipayBill(billDate);
            } else {
                throw new IllegalArgumentException("不支持的渠道: " + channel);
            }
        } catch (Exception e) {
            log.error("[对账] 拉取渠道账单失败 channel={} date={} err={}", channel, billDate, e.getMessage(), e);
            throw new RuntimeException("拉取渠道账单失败: " + e.getMessage(), e);
        }

        // 物理删除当日旧数据（唯一键不含 del_flag，见 agents.md §3）
        baseMapper.purgeByChannelAndDate(channel, billDate);

        for (BizPayChannelBill bill : bills) {
            bill.setChannel(channel);
            bill.setBillDate(billDate);
            bill.setImportWay(1);
            bill.setMatchStatus(0);
            baseMapper.insert(bill);
        }

        log.info("[对账] 导入渠道账单完成 channel={} date={} count={}", channel, billDate, bills.size());
        return bills.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoMatch(Integer channel, LocalDate billDate) {
        List<BizPayChannelBill> pendingBills = baseMapper.selectList(new LambdaQueryWrapper<BizPayChannelBill>()
                .eq(BizPayChannelBill::getChannel, channel)
                .eq(BizPayChannelBill::getBillDate, billDate)
                .eq(BizPayChannelBill::getMatchStatus, 0));

        int matchedCount = 0;
        for (BizPayChannelBill bill : pendingBills) {
            BizPaymentTxn txn = findLocalTxn(channel, bill);
            if (txn != null && amountEquals(txn.getAmount(), bill.getAmount())) {
                bill.setMatchStatus(1);
                bill.setLocalTxnNo(txn.getTxnNo());
                bill.setLocalTxnId(txn.getId());
                bill.setTxnDirection(txn.getDirection());
                bill.setDiffAmount(BigDecimal.ZERO);
                bill.setMatchTime(LocalDateTime.now());
                bill.setMatchedById(UserUtils.getCurrentUser().getEmployeeId());
                bill.setMatchedByName(UserUtils.getCurrentUser().getRealName());
                baseMapper.updateById(bill);
                matchedCount++;
            } else {
                bill.setMatchStatus(2); // 长款：渠道有本地无
                bill.setDiffAmount(bill.getAmount());
                baseMapper.updateById(bill);
            }
        }

        log.info("[对账] 自动勾对完成 channel={} date={} matched={}/{}", channel, billDate, matchedCount, pendingBills.size());
        return matchedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualRegister(PayChannelManualDTO dto) {
        BizPayChannelBill bill = new BizPayChannelBill();
        bill.setChannel(dto.getChannel());
        bill.setBillDate(dto.getTradeTime().toLocalDate());
        bill.setChannelTradeNo(dto.getChannelTradeNo());
        bill.setTradeTime(dto.getTradeTime());
        bill.setAmount(dto.getAmount());
        bill.setImportWay(2);
        bill.setMatchStatus(0);
        baseMapper.insert(bill);
        log.info("[对账] 手工登记渠道流水 channel={} tradeNo={} amount={}", dto.getChannel(), dto.getChannelTradeNo(), dto.getAmount());
    }

    private List<BizPayChannelBill> fetchAlipayBill(LocalDate billDate) throws AlipayApiException {
        log.info("[支付宝对账] 拉取支付宝账单 date={}", billDate);

        AlipayClient client = new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                "json", "UTF-8",
                alipayProperties.getAlipayPublicKey(),
                "RSA2"
        );

        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        String bizContent = String.format(
                "{\"bill_type\":\"trade\",\"bill_date\":\"%s\"}",
                billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
        request.setBizContent(bizContent);

        AlipayDataDataserviceBillDownloadurlQueryResponse response = client.execute(request);

        List<BizPayChannelBill> bills = new ArrayList<>();
        if (response.isSuccess() && response.getBillDownloadUrl() != null) {
            // 下载 CSV 文件并解析
            try {
                URL url = new URL(response.getBillDownloadUrl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                boolean skipHeader = true;
                while ((line = reader.readLine()) != null) {
                    if (skipHeader) {
                        skipHeader = false;
                        continue;
                    }
                    String[] fields = line.split(",", -1);
                    if (fields.length >= 15) {
                        BizPayChannelBill bill = parseAlipayCsvRow(fields);
                        if (bill != null) {
                            bills.add(bill);
                        }
                    }
                }
                reader.close();
            } catch (Exception e) {
                log.error("[支付宝对账] 解析CSV失败 err={}", e.getMessage(), e);
            }
        }

        return bills;
    }

    private BizPayChannelBill parseAlipayCsvRow(String[] fields) {
        try {
            BizPayChannelBill bill = new BizPayChannelBill();
            bill.setChannelTradeNo(fields[1].replaceAll("\"", "")); // 支付宝交易号
            bill.setTradeTime(parseAlipayTime(fields[2])); // 创建时间
            bill.setAmount(new BigDecimal(fields[4].replaceAll("\"", ""))); // 金额
            return bill;
        } catch (Exception e) {
            log.warn("[支付宝对账] 解析行失败: {}", String.join(",", fields));
            return null;
        }
    }

    private LocalDateTime parseAlipayTime(String timeStr) {
        try {
            return LocalDateTime.parse(timeStr.replaceAll("\"", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }

    private BizPaymentTxn findLocalTxn(Integer channel, BizPayChannelBill bill) {
        Integer payMethod = channel == 1 ? 2 : 3; // 微信→2, 支付宝→3
        return bizPaymentTxnMapper.selectOne(new LambdaQueryWrapper<BizPaymentTxn>()
                .eq(BizPaymentTxn::getChannelTxnNo, bill.getChannelTradeNo())
                .eq(BizPaymentTxn::getPayMethod, payMethod)
                .last("LIMIT 1"));
    }

    private boolean amountEquals(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) == 0;
    }
}
