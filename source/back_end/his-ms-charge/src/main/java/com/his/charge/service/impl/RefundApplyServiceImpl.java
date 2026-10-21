package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.dto.BillRefundDTO;
import com.his.charge.dto.RefundApplyItemDTO;
import com.his.charge.dto.RefundApplySubmitDTO;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.entity.BizRefundApply;
import com.his.charge.entity.BizRefundApplyItem;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.mapper.BizPaymentTxnMapper;
import com.his.charge.mapper.BizRefundApplyItemMapper;
import com.his.charge.mapper.BizRefundApplyMapper;
import com.his.charge.service.*;
import com.his.charge.vo.BizRefundApplyVO;
import com.his.charge.vo.RefundableLineVO;
import com.his.common.base.PageResult;
import com.his.common.enums.BillStatusEnum;
import com.his.common.enums.RefundApplyStatusEnum;
import com.his.common.enums.TxnSourceEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.spi.service.RedisSequenceService;
import com.his.common.entity.CurrentUser;
import com.his.common.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 退费申请服务实现（审批台账，不是资金事实）。
 */
@Service
@RequiredArgsConstructor
public class RefundApplyServiceImpl extends ServiceImpl<BizRefundApplyMapper, BizRefundApply> implements RefundApplyService {

    /**
     * cancel_reason 列宽
     */
    private static final int W_CANCEL_REASON = 200;
    /**
     * item_name 列宽（明细行快照）
     */
    private static final int W_ITEM_NAME = 200;
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
    private final BizPaymentTxnMapper bizPaymentTxnMapper;
    /**
     * 发起前的药品退费闸（L1 记账行的来源锚点 + 发药状态，见 sql/154）
     */
    private final FeeRecordService feeRecordService;
    private final SourceAdvanceService sourceAdvanceService;
    private final RedisSequenceService redisSequenceService;
    /**
     * 申请明细行（圈选结果）：发起时落、执行时读、详情出参带
     */
    private final BizRefundApplyItemMapper bizRefundApplyItemMapper;

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
        RefundApplyStatusEnum current = RefundApplyStatusEnum.fromCode(applyStatus);
        return "当前状态是「" + (current == null ? applyStatus : current.getDesc()) + "」，不允许" + action
                + "（审核只认待审核、执行只认审核通过、作废只认待审核或审核通过）";
    }

    @Override
    public PageResult<BizRefundApplyVO> selectRefundApplyPage(Long patientId, Integer applyStatus,
                                                              String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<BizRefundApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizRefundApply::getPatientId, patientId)
                .eq(applyStatus != null, BizRefundApply::getApplyStatus, applyStatus)
                .and(TextUtil.hasText(keyword), w -> w.like(BizRefundApply::getRefundApplyNo, keyword)
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
     * 明细行（圈选结果）同口径：一次 in 捞回，列表与详情都带。
     */
    private List<BizRefundApplyVO> toVOList(List<BizRefundApply> applies) {
        List<BizRefundApplyVO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(applies)) {
            return result;
        }
        Map<Long, List<BizPaymentTxn>> refunds = new HashMap<>();
        List<BizPaymentTxn> txns = bizPaymentTxnMapper.selectRefundsByApplyIds(
                applies.stream().map(BizRefundApply::getId).collect(Collectors.toList()));
        for (BizPaymentTxn txn : txns) {
            refunds.computeIfAbsent(txn.getApplyId(), k -> new ArrayList<>()).add(txn);
        }
        Map<Long, List<BizRefundApplyItem>> itemMap = new HashMap<>();
        List<BizRefundApplyItem> itemRows = bizRefundApplyItemMapper.selectList(new LambdaQueryWrapper<BizRefundApplyItem>()
                .in(BizRefundApplyItem::getApplyId, applies.stream().map(BizRefundApply::getId).collect(Collectors.toList()))
                .orderByAsc(BizRefundApplyItem::getId));
        for (BizRefundApplyItem row : itemRows) {
            itemMap.computeIfAbsent(row.getApplyId(), k -> new ArrayList<>()).add(row);
        }
        for (BizRefundApply apply : applies) {
            BizRefundApplyVO vo = new BizRefundApplyVO();
            BeanUtils.copyProperties(apply, vo);
            vo.setItems(itemMap.get(apply.getId()));
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
                        .filter(TextUtil::hasText)
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
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizRefundApply apply = new BizRefundApply();
        BeanUtils.copyProperties(submitDTO, apply);
        BizSettlementBill bill = settlementBillService.getById(apply.getBillId());
        if (bill == null) {
            throw new BusinessException("原结算账单不存在，不能发起退费申请");
        }
        if (BillStatusEnum.VOIDED.getCode().equals(bill.getBillStatus())) {
            throw new BusinessException("该账单已作废（取消结算），没有收过钱，也就没有可退的钱");
        }
        apply.setBillNo(TextUtil.hasText(apply.getBillNo()) ? apply.getBillNo() : bill.getBillNo());
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

        // 两条路径：
        // ① 带了明细行（新）= 按来源单据圈选：退哪张处方的哪几味药、各退几盒都定格在申请单上，
        //    金额按行快照汇总（前端传来的金额只作展示，以服务端算的为准）；
        // ② 没带明细行（旧）= 金额必须落得到整行边界，执行时按金额贪心凑行。
        List<BizRefundApplyItem> applyItems = new ArrayList<>();
        List<RefundableLineVO> targets;
        if (!CollectionUtils.isEmpty(submitDTO.getItems())) {
            Map<Long, RefundableLineVO> lineByFee = new HashMap<>();
            for (RefundableLineVO line : lines) {
                lineByFee.put(line.getFeeRecordId(), line);
            }
            // 同一行勾两次 = 执行时对同一笔记账行冲两次，第二笔必被剩余量拦下，
            // 但那是执行期才炸；发起时就按行去重，报错也说清楚。
            Set<Long> seen = new HashSet<>();
            for (RefundApplyItemDTO item : submitDTO.getItems()) {
                if (!seen.add(item.getFeeRecordId())) {
                    throw new BusinessException("同一记账行勾选了多次（feeRecordId=" + item.getFeeRecordId() + "），请合并成一行");
                }
            }
            BigDecimal total = BigDecimal.ZERO;
            for (RefundApplyItemDTO item : submitDTO.getItems()) {
                RefundableLineVO line = lineByFee.get(item.getFeeRecordId());
                if (line == null) {
                    throw new BusinessException("记账行 " + item.getFeeRecordId()
                            + " 不在本账单该退费类型的可退清单内（可能已退完或类型不符），可退明细：" + describeLines(lines));
                }
                // 没给数量 = 整行退；给了就按剩余量兜底（共 5 盒不能退 6 盒）
                BigDecimal qty = item.getRefundQuantity() == null ? line.getQuantity() : item.getRefundQuantity();
                if (qty.signum() <= 0) {
                    throw new BusinessException("「" + line.getItemName() + "」退费数量必须大于 0");
                }
                if (qty.compareTo(line.getQuantity()) > 0) {
                    throw new BusinessException("「" + line.getItemName() + "」剩余可退 "
                            + line.getQuantity().stripTrailingZeros().toPlainString()
                            + "，不能退 " + qty.stripTrailingZeros().toPlainString());
                }
                BigDecimal amount = qty.compareTo(line.getQuantity()) >= 0
                        ? line.getAmount()
                        : NumUtil.scale(NumUtil.orZero(line.getPrice()).multiply(qty), AMOUNT_SCALE);
                total = total.add(amount);
                BizRefundApplyItem row = new BizRefundApplyItem();
                row.setFeeRecordId(line.getFeeRecordId());
                row.setRefundQuantity(qty);
                row.setRefundAmount(amount);
                row.setItemName(TextUtil.cut(line.getItemName(), W_ITEM_NAME));
                row.setSourceNo(line.getSourceNo());
                applyItems.add(row);
            }
            apply.setRefundAmount(NumUtil.scale(total, AMOUNT_SCALE));
            targets = submitDTO.getItems().stream()
                    .map(item -> lineByFee.get(item.getFeeRecordId()))
                    .collect(Collectors.toList());
        } else {
            // 金额还得**落得到明细边界**（一张 ¥10+¥20 的账单申请退 ¥15 是退不动的：只能整行退）。
            // 这一句必须放在发起时：等收费处点执行才发现，改金额的口子只剩「作废后重发」。
            targets = pickRefundTargets(lines, apply.getRefundAmount());
        }
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
        apply.setRefundApplyNo(redisSequenceService.generateRefundApplyNo());
        apply.setApplyStatus(RefundApplyStatusEnum.PENDING_AUDIT.getCode());
        apply.setApplyTime(LocalDateTime.now());
        // 申请人以服务端登录身份为准，前端传什么都不采信（避免冒名提交）
        apply.setApplyBy(operatorUser.getRealName());
        this.save(apply);
        // 明细行回填 applyId 后落库：审批单上能看到"退了哪几行、各退多少"，执行时也按它退
        for (BizRefundApplyItem row : applyItems) {
            row.setApplyId(apply.getId());
            bizRefundApplyItemMapper.insert(row);
        }
        BizRefundApplyVO vo = new BizRefundApplyVO();
        BeanUtils.copyProperties(apply, vo);
        vo.setItems(applyItems);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditRefundApply(Long applyId, boolean approved, Long auditorId, String auditorName, String remark) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizRefundApply apply = requireApply(applyId);
        if (!Objects.equals(RefundApplyStatusEnum.PENDING_AUDIT.getCode(), apply.getApplyStatus())) {
            throw new BusinessException(statusError("审核", apply.getApplyStatus()));
        }

        apply.setApplyStatus(approved
                ? RefundApplyStatusEnum.AUDIT_PASSED.getCode() : RefundApplyStatusEnum.AUDIT_REJECTED.getCode());
        // 审核人以服务端登录身份为准，DTO 里带的值只作无登录态时的兜底
        apply.setAuditorId(operatorUser.getEmployeeId());
        apply.setAuditorName(operatorUser.getRealName());
        apply.setAuditTime(LocalDateTime.now());
        apply.setAuditRemark(remark);
        // 守卫下沉到 WHERE：读-判-写之间并发状态变更会使 Java 侧判断失效
        int rows = baseMapper.update(apply, new LambdaUpdateWrapper<BizRefundApply>()
                .eq(BizRefundApply::getId, apply.getId())
                .eq(BizRefundApply::getApplyStatus, RefundApplyStatusEnum.PENDING_AUDIT.getCode()));
        if (rows == 0) {
            throw new BusinessException(statusError("审核", apply.getApplyStatus()));
        }
        return rows > 0;
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
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizRefundApply apply = requireApply(applyId);
        if (!RefundApplyStatusEnum.isInflight(apply.getApplyStatus())) {
            throw new BusinessException(statusError("作废", apply.getApplyStatus()));
        }
        // C 类保留：入参是拆开的 String 原因（Controller 解 DTO 后调用），Bean Validation 不经过这一层
        if (!TextUtil.hasText(reason)) {
            throw new BusinessException("请填写作废原因（台账要能回答「为什么批了又退回去」）");
        }
        apply.setApplyStatus(RefundApplyStatusEnum.DISCARDED.getCode());
        apply.setCancelBy(operatorUser.getRealName());
        apply.setCancelTime(LocalDateTime.now());
        // 先截到列宽再落库：超长会让这句"作废"本身变成 500，用户连申请单都关不掉
        apply.setCancelReason(TextUtil.cut(reason.trim(), W_CANCEL_REASON));
        // 守卫下沉到 WHERE：读-判-写之间并发状态变更会使 Java 侧判断失效
        int rows = baseMapper.update(apply, new LambdaUpdateWrapper<BizRefundApply>()
                .eq(BizRefundApply::getId, apply.getId())
                .in(BizRefundApply::getApplyStatus,
                        RefundApplyStatusEnum.PENDING_AUDIT.getCode(), RefundApplyStatusEnum.AUDIT_PASSED.getCode()));
        if (rows == 0) {
            throw new BusinessException(statusError("作废", apply.getApplyStatus()));
        }
        return rows > 0;
    }

    /**
     * 执行退费 = <b>真的把钱退出去</b>，但本方法一行钱都不碰：
     * 按「类型 + 金额」在账单里选出要整行红冲的记账行，然后交给支付层。
     *
     * <p>{@link PaymentService#refund} 在一个事务里做完：红冲 L1 记账行 → 按原收款流水逐路退款
     * → 刷新账单镜像 → 把来源单据（处方/申请单/发药）退回未缴费 → 作废或重置医保清单。
     * 任何一步失败全部回滚，申请单也就停在「审核通过」，收费处看到的就是原始原因，改完再点一次。
     *
     * <p>跨期退费怎么记账：退款流水写的是<b>当前时间</b>与操作人，所以这笔退费天然落在
     * <b>操作人当日</b>的日结/班结里，已经结掉的过去日不受影响（标准 HIS 的"冲当期"口径）。
     */
    @Override
    public List<RefundableLineVO> refundableLines(Long billId, Integer refundType) {
        return settlementBillService.refundableLines(billId, refundType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeRefund(Long applyId, String refundBy) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizRefundApply apply = requireApply(applyId);
        if (!Objects.equals(RefundApplyStatusEnum.AUDIT_PASSED.getCode(), apply.getApplyStatus())) {
            throw new BusinessException(statusError("退费", apply.getApplyStatus()));
        }
        if (apply.getBillId() == null) {
            throw new BusinessException("该申请没有关联原结算账单，无法退费（请驳回后由收费处按账单重新发起）");
        }

        // 明细行圈选的申请单：按发起时定格的行与数量退。执行前若又被别的退费冲过，
        // reversePartialForRefund 会按当时剩余量当场拒（报错带剩余量），不会静默多退。
        // 没有明细行的存量申请单：回退旧的「金额贪心凑整行」，行为不变。
        List<BizRefundApplyItem> applyItems = bizRefundApplyItemMapper.selectList(new LambdaQueryWrapper<BizRefundApplyItem>()
                .eq(BizRefundApplyItem::getApplyId, apply.getId())
                .orderByAsc(BizRefundApplyItem::getId));

        BillRefundDTO dto = new BillRefundDTO();
        dto.setBillId(apply.getBillId());
        dto.setReason(TextUtil.cut("退费申请 " + apply.getRefundApplyNo() + "：" + apply.getRefundReason(), 500));
        dto.setSourceType(TxnSourceEnum.REFUND_APPLY.getCode());
        dto.setApplyId(apply.getId());
        dto.setApplyNo(apply.getRefundApplyNo());
        BigDecimal actualRefund;
        if (!applyItems.isEmpty()) {
            List<BillRefundDTO.RefundLine> refundLines = new ArrayList<>(applyItems.size());
            for (BizRefundApplyItem item : applyItems) {
                BillRefundDTO.RefundLine line = new BillRefundDTO.RefundLine();
                line.setFeeId(item.getFeeRecordId());
                line.setQuantity(item.getRefundQuantity());
                refundLines.add(line);
            }
            dto.setLines(refundLines);
            actualRefund = applyItems.stream()
                    .map(BizRefundApplyItem::getRefundAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            List<RefundableLineVO> candidates = settlementBillService.refundableLines(apply.getBillId(), apply.getRefundType());
            List<RefundableLineVO> targets = pickRefundTargets(candidates, apply.getRefundAmount());
            dto.setFeeIds(targets.stream().map(RefundableLineVO::getFeeRecordId).collect(Collectors.toList()));
            actualRefund = sumAmount(targets);
        }
        paymentService.refund(dto);

        apply.setApplyStatus(RefundApplyStatusEnum.REFUNDED.getCode());
        // 实退以选中的记账行为准（发起之后可能又有别的退费执行过，前缀和会重新算）
        apply.setRefundAmount(NumUtil.scale(actualRefund, AMOUNT_SCALE));
        apply.setRefundBy(operatorUser.getRealName());
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

}
