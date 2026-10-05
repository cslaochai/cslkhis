package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.dto.BillRefundDTO;
import com.his.charge.dto.RefundApplySubmitDTO;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.entity.BizRefundApply;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.mapper.BizPaymentTxnMapper;
import com.his.charge.mapper.BizRefundApplyMapper;
import com.his.charge.service.PaymentService;
import com.his.charge.service.RefundApplyService;
import com.his.charge.service.SettlementBillService;
import com.his.charge.service.SourceAdvanceService;
import com.his.charge.vo.BizRefundApplyVO;
import com.his.charge.vo.RefundableLineVO;
import com.his.common.base.PageResult;
import com.his.common.enums.BillStatusEnum;
import com.his.common.enums.RefundApplyStatusEnum;
import com.his.common.enums.TxnSourceEnum;
import com.his.common.exception.BusinessException;
import com.his.fee.service.FeeRecordService;
import com.his.security.entity.CurrentUser;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 退费申请服务实现（审批台账，不是资金事实）。
 *
 * <p>四层模型下这张单子只回答三件事：<b>要退哪张账单（L2）的哪一类钱、退多少、谁批的</b>。
 * 钱怎么出去的一律由 {@link PaymentService#refund} 写 L3 退款流水并红冲 L1 记账行，
 * 本服务不再自己碰旧收费单那套状态列 —— 旧实现里"申请单说已退费、
 * 收费单还挂着已收费"的两本账，就是因为两边各写了一套。
 *
 * <p>发起与执行用<b>同一份</b>可退清单判金额（{@link SettlementBillService#refundableLines}），
 * 否则窗口说能退、收费处点执行才被打回。
 */
@Service
@RequiredArgsConstructor
public class RefundApplyServiceImpl extends ServiceImpl<BizRefundApplyMapper, BizRefundApply> implements RefundApplyService {

    private static final AtomicInteger SEQ = new AtomicInteger(0);
    /**
     * cancel_reason 列宽
     */
    private static final int W_CANCEL_REASON = 200;
    private static final int AMOUNT_SCALE = 2;

    /**
     * 账单与它的可退行（发起校验、执行选行都读这里）
     */
    private final SettlementBillService settlementBillService;
    /**
     * 执行退费 = 把动作全权交给支付层：红冲记账行 + 逐笔原路退回 + 推进来源单据 + 联动医保清单
     */
    private final PaymentService paymentService;
    /**
     * 列表/详情回显"钱是怎么退出去的"（退款流水在 L3，一笔申请可能拆成多笔）
     */
    private final BizPaymentTxnMapper paymentTxnMapper;
    /**
     * 发起前的药品退费闸（L1 记账行的来源锚点 + 发药状态，见 sql/154）
     */
    private final FeeRecordService feeRecordService;
    private final SourceAdvanceService sourceAdvanceService;

    private static String describeLines(List<RefundableLineVO> lines) {
        return lines.stream()
                .map(l -> l.getItemName() + " ¥" + l.getAmount().setScale(AMOUNT_SCALE, RoundingMode.HALF_UP).toPlainString())
                .collect(Collectors.joining("、"));
    }

    private static BigDecimal sumAmount(List<RefundableLineVO> lines) {
        return lines.stream().map(l -> l.getAmount() == null ? BigDecimal.ZERO : l.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 状态不对时的统一文案：把当前状态名字打出来，否则用户只知道"不允许"、不知道下一步该点哪。
     */
    private static String statusError(String action, Integer applyStatus) {
        RefundApplyStatusEnum current = RefundApplyStatusEnum.getByCode(applyStatus);
        return "当前状态是「" + (current == null ? applyStatus : current.getDesc()) + "」，不允许" + action
                + "（审核只认待审核、执行只认审核通过、作废只认待审核或审核通过）";
    }

    private static String cut(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    @Override
    public PageResult<BizRefundApplyVO> selectRefundApplyPage(Long patientId, Integer applyStatus,
                                                              String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<BizRefundApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizRefundApply::getPatientId, patientId)
                .eq(applyStatus != null, BizRefundApply::getApplyStatus, applyStatus)
                .and(StringUtils.hasText(keyword), w -> w.like(BizRefundApply::getRefundApplyNo, keyword)
                        .or().like(BizRefundApply::getBillNo, keyword)
                        .or().like(BizRefundApply::getPatientName, keyword))
                // create_time 大面积重复，必须补 id 二级键，否则分页会重复/漏行
                .orderByDesc(BizRefundApply::getCreateTime)
                .orderByDesc(BizRefundApply::getId);

        Page<BizRefundApply> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<BizRefundApplyVO> records = toVOList(page.getRecords());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public BizRefundApplyVO getRefundApplyDetail(Long applyId) {
        BizRefundApply apply = this.getById(applyId);
        if (apply == null) {
            throw new BusinessException("退费申请不存在");
        }
        return toVOList(List.of(apply)).get(0);
    }

    /**
     * 申请单 + 它执行出去的退款流水（执行过才有）。逐行查会打成 N+1，一次 in 捞回来再分组。
     */
    private List<BizRefundApplyVO> toVOList(List<BizRefundApply> applies) {
        List<BizRefundApplyVO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(applies)) {
            return result;
        }
        Map<Long, List<BizPaymentTxn>> refunds = new HashMap<>();
        List<BizPaymentTxn> txns = paymentTxnMapper.selectRefundsByApplyIds(
                applies.stream().map(BizRefundApply::getId).collect(Collectors.toList()));
        for (BizPaymentTxn txn : txns) {
            refunds.computeIfAbsent(txn.getApplyId(), k -> new ArrayList<>()).add(txn);
        }
        for (BizRefundApply apply : applies) {
            BizRefundApplyVO vo = new BizRefundApplyVO();
            BeanUtils.copyProperties(apply, vo);
            List<BizPaymentTxn> rows = refunds.get(apply.getId());
            if (!CollectionUtils.isEmpty(rows)) {
                BizPaymentTxn first = rows.get(0);
                vo.setFlowTxnCount(rows.size());
                vo.setFlowRefundAmount(rows.stream()
                        .map(t -> t.getAmount() == null ? BigDecimal.ZERO : t.getAmount().abs())
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(AMOUNT_SCALE, RoundingMode.HALF_UP));
                vo.setFlowRefundNo(first.getTxnNo());
                vo.setFlowRefundMethod(first.getRefundMethod());
                vo.setFlowPayMethod(first.getPayMethod());
                vo.setFlowChannelRefundNo(rows.stream()
                        .map(BizPaymentTxn::getChannelTxnNo)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.joining(" / ")));
                vo.setFlowInsuranceCancelled(rows.stream().anyMatch(t -> Integer.valueOf(1).equals(t.getInsuranceCancelled())) ? 1 : 0);
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizRefundApplyVO submitRefundApply(RefundApplySubmitDTO submitDTO) {
        BizRefundApply apply = new BizRefundApply();
        BeanUtils.copyProperties(submitDTO, apply);
        BizSettlementBill bill = settlementBillService.getById(apply.getBillId());
        if (bill == null) {
            throw new BusinessException("原结算账单不存在，不能发起退费申请");
        }
        if (BillStatusEnum.VOIDED.getCode().equals(bill.getBillStatus())) {
            throw new BusinessException("该账单已作废（取消结算），没有收过钱，也就没有可退的钱");
        }
        apply.setBillNo(StringUtils.hasText(apply.getBillNo()) ? apply.getBillNo() : bill.getBillNo());
        // 患者快照一律以账单为准：申请单上写着"张三"而账单是李四，审核就成了摆设
        apply.setPatientId(bill.getPatientId());
        apply.setPatientNo(bill.getPatientNo());
        apply.setPatientName(bill.getPatientName());

        // 上限按「该类型还能退多少」而不是账单应缴：一部分退过的单（账单仍是已支付）按应缴算会超退，
        // 而「退药」类型配一张只有检查费的账单，发起时就该拒，而不是等收费处点执行才发现退不动。
        List<RefundableLineVO> lines = settlementBillService.refundableLines(apply.getBillId(), apply.getRefundType());
        BigDecimal refundable = sumAmount(lines);
        if (refundable.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("该账单按此退费类型已经没有可退金额（已全部退过，或这张账单里没有该类费用）");
        }
        if (apply.getRefundAmount() == null || apply.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("退费金额必须大于 0");
        }
        if (apply.getRefundAmount().compareTo(refundable) > 0) {
            throw new BusinessException("退费金额不能超过当前可退金额 ¥" + refundable.toPlainString());
        }
        // 金额还得**落得到明细边界**（一张 ¥10+¥20 的账单申请退 ¥15 是退不动的：只能整行退）。
        // 这一句必须放在发起时：等收费处点执行才发现，改金额的口子只剩「作废后重发」。
        List<RefundableLineVO> targets = pickRefundTargets(lines, apply.getRefundAmount());
        // 药品闸门同理由放在发起时：患者人还在窗口，这时候让他先去办退药最省事。
        // 执行时 PaymentService.refund 还会再挡一次（申请通过到执行之间药可能才被发出去）。
        sourceAdvanceService.assertDrugReturnedForRefund(
                feeRecordService.listByIds(targets.stream()
                        .map(RefundableLineVO::getFeeRecordId).collect(Collectors.toList())), "发起退费申请");

        // 同一张账单只允许有**一条没走完的**申请：待审核(1)、已通过待执行(2)。
        // 连点两下就会生成两条待审核，收费处两条都点「执行」= 同一笔钱退两次，且没有任何地方报错。
        //
        // 已退费(4) 的**不再拦**：一张账单可以分几次退（先退药、隔天患者回来再退检查），
        // 拦死就等于逼收费处把合法的续退打成"重复申请"。防超退不靠这条，
        // 靠上面那句「不能超过当前可退金额」+ 执行时按明细边界的实退核算。
        // 被驳回(3) 与已作废(5) 的同样不算 —— 作废就是把这张账单从锁里放出来。
        LambdaQueryWrapper<BizRefundApply> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(BizRefundApply::getBillId, apply.getBillId())
                .in(BizRefundApply::getApplyStatus,
                        RefundApplyStatusEnum.PENDING_AUDIT.getCode(), RefundApplyStatusEnum.AUDIT_PASSED.getCode());
        if (this.count(dupWrapper) > 0) {
            throw new BusinessException("该账单已有退费申请（待审核 / 待执行），不能重复发起；"
                    + "金额或类型要改，请先作废那条申请");
        }
        apply.setRefundApplyNo("RA" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", SEQ.incrementAndGet() % 10000));
        apply.setApplyStatus(RefundApplyStatusEnum.PENDING_AUDIT.getCode());
        apply.setApplyTime(LocalDateTime.now());
        // 申请人以服务端登录身份为准，前端传什么都不采信（避免冒名提交）
        apply.setApplyBy(StringUtils.hasText(currentUserName()) ? currentUserName() : apply.getApplyBy());
        this.save(apply);
        BizRefundApplyVO vo = new BizRefundApplyVO();
        BeanUtils.copyProperties(apply, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditRefundApply(Long applyId, boolean approved, Long auditorId, String auditorName, String remark) {
        BizRefundApply apply = requireApply(applyId);
        if (!Objects.equals(RefundApplyStatusEnum.PENDING_AUDIT.getCode(), apply.getApplyStatus())) {
            throw new BusinessException(statusError("审核", apply.getApplyStatus()));
        }

        apply.setApplyStatus(approved
                ? RefundApplyStatusEnum.AUDIT_PASSED.getCode() : RefundApplyStatusEnum.AUDIT_REJECTED.getCode());
        // 审核人以服务端登录身份为准，DTO 里带的值只作无登录态时的兜底
        apply.setAuditorId(Objects.nonNull(currentUserId()) ? currentUserId() : auditorId);
        apply.setAuditorName(StringUtils.hasText(currentUserName()) ? currentUserName() : auditorName);
        apply.setAuditTime(LocalDateTime.now());
        apply.setAuditRemark(remark);
        return this.updateById(apply);
    }

    /**
     * 作废只作用于"还活着"的两态（1-待审核 / 2-审核通过）：
     * 3-已驳回、4-已退费、5-已作废都是终态，再作废一次只会把作废人/时间覆盖掉，把留痕改脏。
     *
     * <p>作废**不动账单、不动钱**：执行过的那一步才冲正，这里只是把申请单从判重的锁里摘出来。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean discardRefundApply(Long applyId, String reason) {
        BizRefundApply apply = requireApply(applyId);
        if (!RefundApplyStatusEnum.isInflight(apply.getApplyStatus())) {
            throw new BusinessException(statusError("作废", apply.getApplyStatus()));
        }
        // C 类保留：入参是拆开的 String 原因（Controller 解 DTO 后调用），Bean Validation 不经过这一层
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("请填写作废原因（台账要能回答「为什么批了又退回去」）");
        }
        apply.setApplyStatus(RefundApplyStatusEnum.DISCARDED.getCode());
        apply.setCancelBy(StringUtils.hasText(currentUserName()) ? currentUserName() : "系统");
        apply.setCancelTime(LocalDateTime.now());
        // 先截到列宽再落库：超长会让这句"作废"本身变成 500，用户连申请单都关不掉
        apply.setCancelReason(cut(reason.trim(), W_CANCEL_REASON));
        return this.updateById(apply);
    }

    /**
     * 执行退费 = <b>真的把钱退出去</b>，但本方法一行钱都不碰：
     * 按「类型 + 金额」在账单里选出要整行红冲的记账行，然后交给支付层。
     *
     * <p>{@link PaymentService#refund} 在一个事务里做完：红冲 L1 记账行 → 按原收款流水逐路退款（L3）
     * → 刷新账单镜像 → 把来源单据（处方/申请单/发药）退回未缴费 → 作废或重置医保清单。
     * 任何一步失败全部回滚，申请单也就停在「审核通过」，收费处看到的就是原始原因，改完再点一次。
     *
     * <p>跨期退费怎么记账：退款流水写的是<b>当前时间</b>与操作人，所以这笔退费天然落在
     * <b>操作人当日</b>的日结/班结里，已经结掉的过去日不受影响（标准 HIS 的"冲当期"口径）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeRefund(Long applyId, String refundBy) {
        BizRefundApply apply = requireApply(applyId);
        if (!Objects.equals(RefundApplyStatusEnum.AUDIT_PASSED.getCode(), apply.getApplyStatus())) {
            throw new BusinessException(statusError("退费", apply.getApplyStatus()));
        }
        if (apply.getBillId() == null) {
            throw new BusinessException("该申请没有关联原结算账单，无法退费（请驳回后由收费处按账单重新发起）");
        }

        List<RefundableLineVO> lines = settlementBillService.refundableLines(apply.getBillId(), apply.getRefundType());
        List<RefundableLineVO> targets = pickRefundTargets(lines, apply.getRefundAmount());

        BillRefundDTO dto = new BillRefundDTO();
        dto.setBillId(apply.getBillId());
        dto.setFeeIds(targets.stream().map(RefundableLineVO::getFeeRecordId).collect(Collectors.toList()));
        dto.setReason(cut("退费申请 " + apply.getRefundApplyNo() + "：" + apply.getRefundReason(), 500));
        dto.setSourceType(TxnSourceEnum.REFUND_APPLY.getCode());
        dto.setApplyId(apply.getId());
        dto.setApplyNo(apply.getRefundApplyNo());
        paymentService.refund(dto);

        apply.setApplyStatus(RefundApplyStatusEnum.REFUNDED.getCode());
        // 实退以选中的记账行为准（发起之后可能又有别的退费执行过，前缀和会重新算）
        apply.setRefundAmount(sumAmount(targets).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP));
        apply.setRefundBy(StringUtils.hasText(currentUserName()) ? currentUserName() : refundBy);
        apply.setRefundTime(LocalDateTime.now());
        return this.updateById(apply);
    }

    private BizRefundApply requireApply(Long applyId) {
        BizRefundApply apply = applyId == null ? null : this.getById(applyId);
        if (apply == null) {
            throw new BusinessException("退费申请不存在");
        }
        return apply;
    }

    /**
     * 按申请金额在候选行里贪心选出「整行退」的那几行。
     *
     * <p>只按整条账单行退：半条行（共 5 盒只退 2 盒）在四层里要靠 {@code feeRecordService.reversePartial}
     * 另写红冲负行，而申请单只有金额没有数量，凑不齐就不许发起 —— 检查/检验的退单本来也是整单级的。
     * 落不到边界时列出<b>全部前缀和</b>（¥10、¥30）而不是"贪心能凑出的那几种"：
     * 窗口要的是"这张账单还能退成哪几个数"，只报 ¥10 会让人以为 ¥30 的整单退也退不了。
     */
    private List<RefundableLineVO> pickRefundTargets(List<RefundableLineVO> lines, BigDecimal refundAmount) {
        if (CollectionUtils.isEmpty(lines)) {
            throw new BusinessException("这张账单该类型的费用已经没有可退明细（可能已全部退过）");
        }
        BigDecimal left = refundAmount;
        List<RefundableLineVO> targets = new ArrayList<>();
        for (RefundableLineVO line : lines) {
            BigDecimal eligible = line.getAmount();
            if (left.compareTo(eligible) < 0) {
                continue;
            }
            targets.add(line);
            left = left.subtract(eligible);
        }
        if (left.compareTo(BigDecimal.ZERO) != 0) {
            List<String> legalAmounts = new ArrayList<>();
            BigDecimal acc = BigDecimal.ZERO;
            for (RefundableLineVO line : lines) {
                acc = acc.add(line.getAmount());
                legalAmounts.add(acc.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP).toPlainString());
            }
            throw new BusinessException("退费金额必须落在账单行边界上（按行顺序整条退），可选金额：¥"
                    + String.join(" / ¥", legalAmounts) + "；可退明细：" + describeLines(lines));
        }
        return targets;
    }

    /**
     * 当前登录人姓名：优先员工姓名，退回登录账号真实姓名
     */
    private String currentUserName() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            return null;
        }
        return StringUtils.hasText(user.getEmployeeName()) ? user.getEmployeeName() : user.getRealName();
    }

    /**
     * 当前登录人ID：优先员工ID（与站内信、签名口径一致），退回登录账号ID
     */
    private Long currentUserId() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            return null;
        }
        return Objects.nonNull(user.getEmployeeId()) ? user.getEmployeeId() : user.getUserId();
    }
}
