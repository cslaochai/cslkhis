package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.dto.*;
import com.his.charge.entity.BizPayChannelBill;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.mapper.BizPayChannelBillMapper;
import com.his.charge.mapper.BizPaymentTxnMapper;
import com.his.charge.service.PayChannelBillService;
import com.his.charge.service.PayChannelService;
import com.his.charge.vo.PayChannelBillVO;
import com.his.charge.vo.PayChannelCandidateVO;
import com.his.charge.vo.PayChannelSummaryVO;
import com.his.common.base.PageResult;
import com.his.common.enums.PayDirectionEnum;
import com.his.common.enums.PayTxnStatusEnum;
import com.his.common.enums.PaymentMethodEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支付渠道对账服务实现（M7 留口子，四层口径）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayChannelBillServiceImpl extends ServiceImpl<BizPayChannelBillMapper, BizPayChannelBill>
        implements PayChannelBillService {

    /**
     * 可对账的在线渠道：取自 {@link PaymentMethodEnum#channelBacked()}，不另写一份裸数字表
     */
    private static final List<Integer> CHANNELS = Arrays.stream(PaymentMethodEnum.values())
            .filter(PaymentMethodEnum::channelBacked).map(PaymentMethodEnum::getCode).toList();

    private final BizPaymentTxnMapper bizPaymentTxnMapper;
    private final PayChannelService payChannelService;

    private static List<PayChannelBillVO> toVOList(List<BizPayChannelBill> bills) {
        List<PayChannelBillVO> vos = new ArrayList<>(bills.size());
        for (BizPayChannelBill bill : bills) {
            PayChannelBillVO vo = new PayChannelBillVO();
            BeanUtils.copyProperties(bill, vo);
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public PageResult<PayChannelBillVO> selectPage(PayChannelQueryPageDTO query) {
        LambdaQueryWrapper<BizPayChannelBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getChannel() != null, BizPayChannelBill::getChannel, query.getChannel())
                .eq(query.getMatchStatus() != null, BizPayChannelBill::getMatchStatus, query.getMatchStatus())
                .eq(query.getBillDate() != null, BizPayChannelBill::getBillDate, query.getBillDate())
                .eq(TextUtil.hasText(query.getLocalTxnNo()), BizPayChannelBill::getLocalTxnNo, query.getLocalTxnNo())
                .like(TextUtil.hasText(query.getChannelTradeNo()), BizPayChannelBill::getChannelTradeNo, query.getChannelTradeNo())
                .orderByDesc(BizPayChannelBill::getBillDate)
                .orderByDesc(BizPayChannelBill::getId);
        Page<BizPayChannelBill> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), toVOList(page.getRecords()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importBill(PayChannelImportDTO dto) {
        requireChannel(dto.getChannel());
        List<PayChannelService.ChannelTrade> trades = payChannelService.fetchChannelBill(dto.getChannel(), dto.getBillDate());
        if (trades == null || trades.isEmpty()) {
            return 0;
        }

        // 幂等：同渠道同日已导入的流水号直接跳过（唯一键 (channel, channel_trade_no) 兜底并发）
        Set<String> exists = this.list(new LambdaQueryWrapper<BizPayChannelBill>()
                        .eq(BizPayChannelBill::getChannel, dto.getChannel())
                        .eq(BizPayChannelBill::getBillDate, dto.getBillDate()))
                .stream().map(BizPayChannelBill::getChannelTradeNo).collect(Collectors.toSet());

        String batchNo = "IMP" + LocalDateTime.now().format(DateFormats.COMPACT_DATETIME)
                + String.format("%03d", dto.getChannel());
        int inserted = 0;
        for (PayChannelService.ChannelTrade trade : trades) {
            if (exists.contains(trade.channelTradeNo())) {
                continue;
            }
            BizPayChannelBill bill = new BizPayChannelBill();
            bill.setChannel(dto.getChannel());
            bill.setBillDate(dto.getBillDate());
            bill.setChannelTradeNo(trade.channelTradeNo());
            bill.setTradeTime(trade.tradeTime() == null ? LocalDateTime.now() : trade.tradeTime());
            bill.setAmount(trade.amount());
            bill.setImportWay(1);
            bill.setMatchStatus(0);
            bill.setImportBatchNo(batchNo);
            try {
                this.save(bill);
                inserted++;
            } catch (DuplicateKeyException e) {
                log.info("渠道流水重复导入跳过 channel={} tradeNo={}", dto.getChannel(), trade.channelTradeNo());
            }
        }
        log.info("[M7渠道对账] 渠道={} 账单日={} 拉取 {} 笔，新落台账 {} 笔（批次 {}）",
                dto.getChannel(), dto.getBillDate(), trades.size(), inserted, batchNo);
        return inserted;
    }

    @Override
    public boolean manualRegister(PayChannelManualDTO dto) {
        requireChannel(dto.getChannel());
        // D 取值规则（非空校验已下沉 DTO @NotNull）：手工登记的金额必须非 0，退款填负数
        if (dto.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("金额不能为 0（退款流水填负数）");
        }
        BizPayChannelBill bill = new BizPayChannelBill();
        bill.setChannel(dto.getChannel());
        bill.setBillDate(dto.getBillDate());
        bill.setChannelTradeNo(dto.getChannelTradeNo().trim());
        bill.setTradeTime(dto.getTradeTime() == null ? LocalDateTime.now() : dto.getTradeTime());
        bill.setAmount(dto.getAmount());
        bill.setImportWay(2);
        bill.setMatchStatus(0);
        bill.setImportBatchNo("MANUAL");
        bill.setRemark(TextUtil.hasText(dto.getRemark()) ? TextUtil.cut(dto.getRemark(), 490) : null);
        try {
            return this.save(bill);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该渠道流水号已存在（同一渠道同一流水号只落一次）：" + dto.getChannelTradeNo());
        }
    }

    @Override
    public boolean match(PayChannelMatchDTO dto) {
        BizPayChannelBill bill = this.getById(dto.getId());
        if (bill == null) {
            throw new BusinessException("台账行不存在");
        }
        if (bill.getMatchStatus() != null && bill.getMatchStatus() != 0) {
            throw new BusinessException("只有待勾对的流水才能勾对（当前状态 " + bill.getMatchStatus() + "）");
        }
        BizPaymentTxn txn = findTxn(dto.getLocalTxnNo().trim());
        if (!PayTxnStatusEnum.SUCCESS.getCode().equals(txn.getTxnStatus())) {
            throw new BusinessException("流水 " + txn.getTxnNo() + " 不是成功状态（当前状态 "
                    + txn.getTxnStatus() + "），已冲正流水不能作为勾对目标");
        }
        if (!bill.getChannel().equals(txn.getPayMethod())) {
            throw new BusinessException("渠道不一致：流水是「" + PaymentMethodEnum.labelOrUnknown(bill.getChannel())
                    + "」，支付流水渠道是「" + PaymentMethodEnum.labelOrUnknown(txn.getPayMethod()) + "」");
        }
        BigDecimal remote = NumUtil.orZero(bill.getAmount());
        BigDecimal local = NumUtil.orZero(txn.getAmount());
        if (remote.compareTo(local) != 0) {
            throw new BusinessException("金额不符，拒绝勾对：渠道 " + remote + " vs 本地 " + local
                    + "，差额 " + remote.subtract(local) + "；如确认为长短款请用「差额处理」登记定性");
        }

        bill.setMatchStatus(1);
        bill.setLocalTxnNo(txn.getTxnNo());
        bill.setLocalTxnId(txn.getId());
        bill.setTxnDirection(txn.getDirection());
        bill.setMatchTime(LocalDateTime.now());
        bill.setMatchedById(UserUtils.getCurrentUser().getEmployeeId());
        bill.setMatchedByName(UserUtils.getCurrentUser().getRealName());
        bill.setDiffAmount(BigDecimal.ZERO);
        try {
            return this.updateById(bill);
        } catch (DuplicateKeyException e) {
            // ux_bill_txn：一笔流水只能被一条台账勾走（并发下另一人先勾了）
            throw new BusinessException("该支付流水已被其他渠道流水勾对：" + txn.getTxnNo());
        }
    }

    @Override
    public boolean handleDiff(PayChannelDiffDTO dto) {
        if (!Integer.valueOf(2).equals(dto.getHandleType()) && !Integer.valueOf(3).equals(dto.getHandleType())) {
            throw new BusinessException("处理结论只支持 2-长款 3-短款");
        }
        BizPayChannelBill bill = this.getById(dto.getId());
        if (bill == null) {
            throw new BusinessException("台账行不存在");
        }
        if (bill.getMatchStatus() != null && bill.getMatchStatus() != 0) {
            throw new BusinessException("该流水已处理（当前状态 " + bill.getMatchStatus() + "），不能重复定性");
        }
        bill.setMatchStatus(dto.getHandleType());
        bill.setMatchTime(LocalDateTime.now());
        bill.setMatchedById(UserUtils.getCurrentUser().getEmployeeId());
        bill.setMatchedByName(UserUtils.getCurrentUser().getRealName());
        bill.setHandleRemark(TextUtil.cut(dto.getHandleRemark(), 490));
        return this.updateById(bill);
    }

    @Override
    public PayChannelSummaryVO summary(LocalDate billDate) {
        LocalDate day = billDate == null ? LocalDate.now() : billDate;
        PayChannelSummaryVO vo = new PayChannelSummaryVO();
        vo.setBillDate(day.toString());

        // 本地口径：当日该渠道的成功支付流水（收正退负，SUM 即净额）
        List<BizPaymentTxn> localTxns = bizPaymentTxnMapper.selectList(new LambdaQueryWrapper<BizPaymentTxn>()
                .in(BizPaymentTxn::getPayMethod, CHANNELS)
                .eq(BizPaymentTxn::getTxnStatus, PayTxnStatusEnum.SUCCESS.getCode())
                .eq(BizPaymentTxn::getTxnDate, day));
        Map<Integer, List<BizPaymentTxn>> localByChannel = localTxns.stream()
                .collect(Collectors.groupingBy(BizPaymentTxn::getPayMethod));

        // 渠道口径：台账按渠道/状态汇总
        List<BizPayChannelBill> bills = this.list(new LambdaQueryWrapper<BizPayChannelBill>()
                .eq(BizPayChannelBill::getBillDate, day));
        Map<Integer, List<BizPayChannelBill>> billByChannel = bills.stream()
                .collect(Collectors.groupingBy(BizPayChannelBill::getChannel));

        List<PayChannelSummaryVO.Row> rows = new ArrayList<>();
        for (Integer channel : CHANNELS) {
            PayChannelSummaryVO.Row row = new PayChannelSummaryVO.Row();
            row.setChannel(channel);
            row.setChannelText(PaymentMethodEnum.labelOrUnknown(channel));

            List<BizPaymentTxn> locals = localByChannel.getOrDefault(channel, List.of());
            row.setLocalCount((long) locals.size());
            row.setLocalAmount(locals.stream().map(t -> NumUtil.orZero(t.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));

            List<BizPayChannelBill> flows = billByChannel.getOrDefault(channel, List.of());
            row.setFlowTotal((long) flows.size());
            List<BizPayChannelBill> matched = flows.stream()
                    .filter(b -> Integer.valueOf(1).equals(b.getMatchStatus())).toList();
            row.setMatchedCount((long) matched.size());
            row.setMatchedAmount(sum(matched));
            List<BizPayChannelBill> unmatched = flows.stream()
                    .filter(b -> !Integer.valueOf(1).equals(b.getMatchStatus())).toList();
            row.setUnmatchedCount((long) unmatched.size());
            row.setUnmatchedAmount(sum(unmatched));

            row.setDiffAmount(row.getFlowTotal() == 0 && row.getLocalCount() == 0
                    ? BigDecimal.ZERO
                    : sum(flows).subtract(row.getLocalAmount()));
            rows.add(row);
        }
        vo.setRows(rows);
        return vo;
    }

    @Override
    public List<PayChannelCandidateVO> matchCandidates(Long channelBillId) {
        BizPayChannelBill bill = this.getById(channelBillId);
        if (bill == null) {
            throw new BusinessException("台账行不存在");
        }
        List<BizPaymentTxn> txns = bizPaymentTxnMapper.selectList(new LambdaQueryWrapper<BizPaymentTxn>()
                .eq(BizPaymentTxn::getPayMethod, bill.getChannel())
                .eq(BizPaymentTxn::getTxnStatus, PayTxnStatusEnum.SUCCESS.getCode())
                .eq(BizPaymentTxn::getTxnDate, bill.getBillDate())
                .orderByAsc(BizPaymentTxn::getTxnTime));

        // 已被本渠道当日其他台账行勾走的流水不再是候选
        Set<String> used = this.list(new LambdaQueryWrapper<BizPayChannelBill>()
                        .eq(BizPayChannelBill::getChannel, bill.getChannel())
                        .eq(BizPayChannelBill::getBillDate, bill.getBillDate())
                        .eq(BizPayChannelBill::getMatchStatus, 1)
                        .ne(BizPayChannelBill::getId, bill.getId()))
                .stream().map(BizPayChannelBill::getLocalTxnNo).collect(Collectors.toSet());

        // 金额相等的排前面（勾对场景绝大多数是金额精确匹配）
        List<BizPaymentTxn> candidates = txns.stream()
                .filter(t -> !used.contains(t.getTxnNo()))
                .collect(Collectors.toCollection(ArrayList::new));
        BigDecimal remote = NumUtil.orZero(bill.getAmount());
        candidates.sort(Comparator.comparing((BizPaymentTxn t) -> NumUtil.orZero(t.getAmount()).compareTo(remote) != 0 ? 1 : 0));
        List<PayChannelCandidateVO> vos = new ArrayList<>(candidates.size());
        for (BizPaymentTxn txn : candidates) {
            PayChannelCandidateVO vo = new PayChannelCandidateVO();
            vo.setTxnNo(txn.getTxnNo());
            vo.setDirection(txn.getDirection());
            vo.setDirectionText(PayDirectionEnum.descOf(txn.getDirection()));
            vo.setBillNo(txn.getBillNo());
            vo.setPatientName(txn.getPatientName());
            vo.setAmount(txn.getAmount());
            vo.setTxnTime(txn.getTxnTime() == null ? "" : DateFormats.DATETIME.format(txn.getTxnTime()));
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 按流水号取支付流水：号段唯一（uk_txn_no），非渠道/余额等一律照常拒绝
     */
    private BizPaymentTxn findTxn(String txnNo) {
        BizPaymentTxn txn = bizPaymentTxnMapper.selectOne(new LambdaQueryWrapper<BizPaymentTxn>()
                .eq(BizPaymentTxn::getTxnNo, txnNo)
                .last("LIMIT 1"));
        if (txn == null) {
            throw new BusinessException("本地支付流水不存在：" + txnNo);
        }
        return txn;
    }

    private void requireChannel(Integer channel) {
        if (channel == null || !CHANNELS.contains(channel)) {
            throw new BusinessException("支付渠道只支持 2-微信 3-支付宝 6-银行卡（4 是医保个人账户，不走商户平台）");
        }
    }

    private BigDecimal sum(List<BizPayChannelBill> bills) {
        return bills.stream().map(b -> NumUtil.orZero(b.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
