package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.his.common.enums.PayChannelEnum;
import com.his.common.enums.PaymentMethodEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.spi.service.AlipayChannelConfigFacadeService;
import com.his.spi.vo.AlipayChannelConfigVO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 支付渠道对账服务实现
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PayChannelReconciliationServiceImpl extends ServiceImpl<BizPayChannelBillMapper, BizPayChannelBill>
        implements PayChannelReconciliationService {

    private final BizPaymentTxnMapper bizPaymentTxnMapper;
    private final AlipayChannelConfigFacadeService alipayChannelConfigFacadeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importChannelBill(Integer channel, LocalDate billDate) {
        AlipayChannelConfigVO alipayConfig = alipayChannelConfigFacadeService.getChannelConfig();
        if (!alipayConfig.isEnabled() && Objects.equals(channel, PayChannelEnum.ALIPAY.getCode())) {
            // 渠道关闭时根本发不出支付宝请求，此时放过去只会在 SDK 那一步炸成 500，
            // 现象与「医院系统坏了」一样，而真因是配置没打开
            throw new BusinessException(
                    "支付宝支付未启用（alipay.enabled=false），无法拉取渠道账单对账。" +
                            "请在 gitignored 的 application-dev.yml 中将 alipay.enabled 改为 true 并配置凭据。"
            );
        }

        List<BizPayChannelBill> bills;
        try {
            if (Objects.equals(channel, PayChannelEnum.WECHAT.getCode())) {
                // TODO: 微信账单下载需要 WxJava 完整实现
                log.warn("[微信支付对账] 拉取微信账单完整实现待补");
                bills = new ArrayList<>();
            } else if (Objects.equals(channel, PayChannelEnum.ALIPAY.getCode())) {
                bills = fetchAlipayBill(billDate, alipayConfig);
            } else {
                throw new BusinessException("不支持的对账渠道：" + PayChannelEnum.getLabelOrUnknown(channel));
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[对账] 拉取渠道账单失败 channel={} date={} err={}", channel, billDate, e.getMessage(), e);
            throw new BusinessException("拉取渠道账单失败：" + e.getMessage());
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
            // 守卫下沉到 WHERE：本批 SELECT（matchStatus=0）到逐条回写之间，其他勾对/差额处理可能已改过这行
            LambdaUpdateWrapper<BizPayChannelBill> pendingGuard = new LambdaUpdateWrapper<BizPayChannelBill>()
                    .eq(BizPayChannelBill::getId, bill.getId())
                    .eq(BizPayChannelBill::getMatchStatus, 0);
            if (txn != null && amountEquals(txn.getAmount(), bill.getAmount())) {
                bill.setMatchStatus(1);
                bill.setLocalTxnNo(txn.getTxnNo());
                bill.setLocalTxnId(txn.getId());
                bill.setTxnDirection(txn.getDirection());
                bill.setDiffAmount(BigDecimal.ZERO);
                bill.setMatchTime(LocalDateTime.now());
                bill.setMatchedById(UserUtils.getCurrentUser().getEmployeeId());
                bill.setMatchedByName(UserUtils.getCurrentUser().getRealName());
                if (baseMapper.update(bill, pendingGuard) == 0) {
                    throw new BusinessException("自动勾对期间台账状态被并发变更（渠道流水号 "
                            + bill.getChannelTradeNo() + "，已非待勾对），请重新执行自动勾对");
                }
                matchedCount++;
            } else {
                bill.setMatchStatus(2); // 长款：渠道有本地无
                bill.setDiffAmount(bill.getAmount());
                if (baseMapper.update(bill, pendingGuard) == 0) {
                    throw new BusinessException("自动勾对期间台账状态被并发变更（渠道流水号 "
                            + bill.getChannelTradeNo() + "，已非待勾对），请重新执行自动勾对");
                }
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

    private List<BizPayChannelBill> fetchAlipayBill(LocalDate billDate, AlipayChannelConfigVO alipayConfig) throws AlipayApiException {
        log.info("[支付宝对账] 拉取支付宝账单 date={}", billDate);

        AlipayClient client = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json", "UTF-8",
                alipayConfig.getAlipayPublicKey(),
                "RSA2"
        );

        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        String bizContent = String.format(
                "{\"bill_type\":\"trade\",\"bill_date\":\"%s\"}",
                billDate.format(DateFormats.DATE)
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
            return LocalDateTime.parse(timeStr.replaceAll("\"", ""), DateFormats.DATETIME);
        } catch (Exception e) {
            return null;
        }
    }

    private BizPaymentTxn findLocalTxn(Integer channel, BizPayChannelBill bill) {
        PaymentMethodEnum payMethod = PayChannelEnum.payMethodOf(channel);
        if (payMethod == null) {
            return null;
        }
        return bizPaymentTxnMapper.selectOne(new LambdaQueryWrapper<BizPaymentTxn>()
                .eq(BizPaymentTxn::getChannelTxnNo, bill.getChannelTradeNo())
                .eq(BizPaymentTxn::getPayMethod, payMethod.getCode())
                .last("LIMIT 1"));
    }

    private boolean amountEquals(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) == 0;
    }
}
