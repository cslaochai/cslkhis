package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.api.PatientGateway;
import com.his.charge.dto.BillQueryPageDTO;
import com.his.charge.dto.BillSettleUpsertDTO;
import com.his.charge.dto.BillVoidDTO;
import com.his.charge.dto.PendingEncounterQueryPageDTO;
import com.his.charge.entity.*;
import com.his.charge.mapper.BizInsuranceCatalogRuleMapper;
import com.his.charge.mapper.BizPaymentTxnMapper;
import com.his.charge.mapper.BizSettlementBillItemMapper;
import com.his.charge.mapper.BizSettlementBillMapper;
import com.his.charge.service.FeeRecordService;
import com.his.charge.service.InsuranceSettlementService;
import com.his.charge.service.SettlementBillService;
import com.his.charge.service.SourceAdvanceService;
import com.his.charge.vo.*;
import com.his.common.base.PageResult;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.system.service.InsurancePolicyService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * 结算账单实现（L2）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementBillServiceImpl extends ServiceImpl<BizSettlementBillMapper, BizSettlementBill>
        implements SettlementBillService {

    private static final int AMOUNT_SCALE = 2;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * 医保目录类别（与记账行同口径）：0-自费 1-甲类 2-乙类 3-丙类
     */
    private static final int CATALOG_SELF_PAY = 0;
    private static final int CATALOG_A = 1;
    private static final int CATALOG_B = 2;
    private static final int CATALOG_C = 3;

    private static final int W_BILL_NO = 32;
    private static final int W_PATIENT_NO = 32;
    private static final int W_PATIENT_NAME = 50;
    private static final int W_ENCOUNTER_NO = 32;
    private static final int W_DEPT_NAME = 100;
    private static final int W_ITEM_CODE = 32;
    private static final int W_ITEM_NAME = 200;
    private static final int W_SPEC = 100;
    private static final int W_UNIT = 20;
    private static final int W_INSURANCE_TYPE = 32;
    private static final int W_BILL_BY_NAME = 64;
    private static final int W_VOID_REASON = 200;
    private static final int W_CLOSE_REASON = 200;
    private static final int W_REMARK = 500;

    private final FeeRecordService feeRecordService;
    private final InsuranceSettlementService insuranceSettlementService;
    private final SourceAdvanceService sourceAdvanceService;
    private final BizSettlementBillItemMapper billItemMapper;
    private final BizPaymentTxnMapper paymentTxnMapper;
    private final PatientGateway patientGateway;
    private final InsurancePolicyService insurancePolicyService;
    private final RedisSequenceService redisSequenceService;
    private final BizInsuranceCatalogRuleMapper catalogRuleMapper;

    private static BigDecimal scale(BigDecimal value) {
        return nz(value).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String cut(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizSettlementBill settle(BillSettleUpsertDTO dto) {
        Draft draft = draft(dto);
        List<BizFeeRecord> rows = draft.rows();
        List<BizSettlementBillItem> items = draft.items();
        PatientInsurance insurance = draft.insurance();
        BigDecimal discount = draft.discount();
        BigDecimal total = draft.total();
        BizFeeRecord first = rows.get(0);

        BizSettlementBill bill = new BizSettlementBill();
        bill.setBillNo(cut(redisSequenceService.generateBillNo(), W_BILL_NO));
        bill.setPatientId(first.getPatientId());
        bill.setPatientNo(cut(first.getPatientNo(), W_PATIENT_NO));
        bill.setPatientName(cut(first.getPatientName(), W_PATIENT_NAME));
        bill.setEncounterType(first.getEncounterType());
        bill.setEncounterId(first.getEncounterId());
        bill.setEncounterNo(cut(first.getEncounterNo(), W_ENCOUNTER_NO));
        bill.setBillType(resolveBillType(dto.getBillType(), first.getEncounterType()).getCode());
        bill.setFeeCount(items.size());
        bill.setTotalAmount(total);
        bill.setDiscountAmount(discount);
        bill.setSettlementMode(insurance.mode().getCode());
        bill.setInsuranceType(cut(insurance.type(), W_INSURANCE_TYPE));
        bill.setPoolAmount(draft.pool());
        bill.setAccountAmount(draft.account());
        bill.setSelfAmount(draft.self());
        bill.setPayableAmount(draft.payable());
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setRefundAmount(BigDecimal.ZERO);
        // 医保全额报销时个人应缴为 0：没有一分钱要收，出账即结清，close_reason 说清为什么"没流水却已支付"
        if (draft.payable().signum() == 0) {
            bill.setBillStatus(BillStatusEnum.PAID.getCode());
            bill.setPayTime(LocalDateTime.now());
            bill.setCloseReason(cut("医保全额报销，个人无应缴", W_CLOSE_REASON));
        } else if (BillTypeEnum.DISCHARGE.getCode().equals(bill.getBillType()) && draft.payable().signum() > 0) {
            // 出院结算且有欠费：设为 6-挂账/欠费，联动住院欠费管控策略欠费追缴流程
            bill.setBillStatus(6);
        } else {
            bill.setBillStatus(BillStatusEnum.UNPAID.getCode());
        }
        bill.setBillDate(LocalDate.now());
        bill.setBillTime(LocalDateTime.now());
        bill.setBillById(UserUtils.getCurrentUser().getEmployeeId());
        bill.setBillByName(cut(UserUtils.getCurrentUser().getRealName(), W_BILL_BY_NAME));
        bill.setRemark(cut(dto.getRemark(), W_REMARK));
        this.save(bill);

        for (BizSettlementBillItem item : items) {
            item.setBillId(bill.getId());
            item.setBillNo(bill.getBillNo());
            billItemMapper.insert(item);
        }
        // 锁定放最后：任何一个环节异常都会整笔回滚，不会留下"账单生成了、费用还没锁"的空账单
        List<Long> feeIds = new ArrayList<>();
        for (BizFeeRecord row : rows) {
            feeIds.add(row.getId());
        }
        feeRecordService.lockToBill(feeIds, bill.getId());

        // 医保清单是账单的产物：有统筹记账才出（全自费/全丙类没有要报给医保的钱）。
        // 放在这里而不是收费完成后 —— 统筹 split 在出账这一刻就已经定死，晚一步就得再算一遍。
        insuranceSettlementService.generateFromBill(bill, items, insurance.coverageRatio());

        // 应缴为 0 的账单不会有收款流水，也就永远不会有人替它跑 refreshFromTxns：
        // 结清与来源推进必须在这里一次做完，否则全额报销的处方永远停在「已锁定 + 未缴费」、药房发不了药
        if (BillStatusEnum.PAID.getCode().equals(bill.getBillStatus())) {
            feeRecordService.markSettledByBill(bill.getId());
            sourceAdvanceService.advanceByBill(bill);
        }

        log.info("[结算] 账单 {} 患者 {} 行 {} 应收 ¥{} 优惠 ¥{} 统筹 ¥{} 应缴 ¥{}",
                bill.getBillNo(), bill.getPatientName(), items.size(), total.toPlainString(),
                discount.toPlainString(), draft.pool().toPlainString(), draft.payable().toPlainString());
        return bill;
    }

    @Override
    public BillPreviewVO previewSettlement(BillSettleUpsertDTO dto) {
        Draft draft = draft(dto);
        BillPreviewVO vo = new BillPreviewVO();
        vo.setEncounterType(dto.getEncounterType());
        vo.setEncounterId(dto.getEncounterId());
        vo.setFeeCount(draft.rows().size());
        vo.setTotalAmount(draft.total());
        vo.setDiscountAmount(draft.discount());
        vo.setPoolAmount(draft.pool());
        vo.setAccountAmount(draft.account());
        vo.setSelfAmount(draft.self());
        vo.setPayableAmount(draft.payable());
        vo.setSettlementMode(draft.insurance().mode().getCode());
        vo.setSettlementModeText(draft.insurance().mode().getDesc());
        vo.setInsuranceType(draft.insurance().type());
        vo.setCoverageRatio(draft.insurance().coverageRatio());
        List<BizSettlementBillItem> items = draft.items();
        vo.setItems(items);
        vo.setPatientId(items.isEmpty() ? null : items.get(0).getPatientId());
        return vo;
    }

    /**
     * 出账前的全部计算：取行 → 校验 → 逐行 split → 摊优惠 → 合计。
     *
     * <p>{@link #settle} 与 {@link #previewSettlement} 共用这一份，所以"试算说该收多少"
     * 与"出账开出多少"必然是同一个数 —— 住院结算原先逐单调旧试算再汇总，
     * 出账时又算一遍，两边一漂移就是小票和结算单对不上。
     */
    private Draft draft(BillSettleUpsertDTO dto) {
        if (dto == null || EncounterTypeEnum.fromCode(dto.getEncounterType()) == null || dto.getEncounterId() == null) {
            throw new BusinessException("缺少就诊标识，无法结算");
        }
        List<BizFeeRecord> rows = pickRows(dto);
        BigDecimal total = BigDecimal.ZERO;
        for (BizFeeRecord row : rows) {
            total = total.add(nz(row.getAmount()));
        }
        total = scale(total);
        if (total.signum() < 0) {
            throw new BusinessException("本次选中费用的净额为负（" + total.toPlainString() + "），红冲行请随其原行一起结算");
        }
        if (total.signum() == 0) {
            // 净额为 0 的账单不产生任何资金事实，落一张空账单只会让日结多一条对不上的记录
            throw new BusinessException("本次结算金额为 0，无需出账（免收请改为不记账，而不是记一笔 0 元）");
        }

        BigDecimal discount = scale(dto.getDiscountAmount() == null ? BigDecimal.ZERO : dto.getDiscountAmount());
        if (discount.signum() < 0) {
            throw new BusinessException("优惠金额不能为负");
        }
        if (discount.compareTo(total) >= 0) {
            throw new BusinessException("优惠不能大于或等于应收合计（全额免单请走「不记账 + 结清说明」，不要出账单）");
        }

        BizFeeRecord first = rows.get(0);
        PatientInsurance insurance = resolveInsurance(first.getPatientId(), dto.getSettlementMode());
        List<BizSettlementBillItem> items = buildItems(first, rows, discount, insurance);

        BigDecimal pool = sum(items, BizSettlementBillItem::getPoolAmount);
        BigDecimal account = sum(items, BizSettlementBillItem::getAccountAmount);
        BigDecimal self = sum(items, BizSettlementBillItem::getSelfAmount);
        BigDecimal payable = scale(total.subtract(discount).subtract(pool).subtract(account));
        return new Draft(rows, items, insurance, total, discount, pool, account, self, payable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidBill(BillVoidDTO dto) {
        if (dto == null || dto.getBillId() == null) {
            throw new BusinessException("缺少账单");
        }
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("缺少作废原因");
        }
        BizSettlementBill bill = this.getById(dto.getBillId());
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        if (BillStatusEnum.VOIDED.getCode().equals(bill.getBillStatus())) {
            return;
        }
        if (nz(bill.getPaidAmount()).subtract(nz(bill.getRefundAmount())).signum() > 0) {
            throw new BusinessException("该账单已有收款，作废会把已收的钱凭空抹掉，请走退费");
        }
        List<BizSettlementBillItem> items = billItemMapper.selectByBill(bill.getId());
        List<Long> feeIds = new ArrayList<>();
        for (BizSettlementBillItem item : items) {
            feeIds.add(item.getFeeRecordId());
        }

        bill.setBillStatus(BillStatusEnum.VOIDED.getCode());
        bill.setVoidById(UserUtils.getCurrentUser().getEmployeeId());
        bill.setVoidByName(cut(UserUtils.getCurrentUser().getRealName(), W_BILL_BY_NAME));
        bill.setVoidTime(LocalDateTime.now());
        bill.setVoidReason(cut(dto.getReason(), W_VOID_REASON));
        this.updateById(bill);

        if (!feeIds.isEmpty()) {
            feeRecordService.releaseFromBill(feeIds);
        }
        // 账单作废必须给医保清单一个出口：已报盘的发 2305 撤回，没报盘的直接置已作废，
        // 否则清单会留在「待结算」列表里，等着被人报一张账单已经不存在的 2304
        insuranceSettlementService.voidByBill(bill.getId(), dto.getReason());
        log.info("[取消结算] 账单 {} 作废，解锁记账行 {} 条，原因：{}", bill.getBillNo(), feeIds.size(), dto.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void linkRedoChain(Long newBillId, Long origBillId) {
        if (newBillId == null || origBillId == null) {
            throw new BusinessException("缺少账单ID");
        }
        BizSettlementBill newBill = this.getById(newBillId);
        if (newBill == null) {
            throw new BusinessException("新账单不存在");
        }
        BizSettlementBill origBill = this.getById(origBillId);
        if (origBill == null) {
            throw new BusinessException("原账单不存在");
        }
        newBill.setOrigBillId(origBillId);
        this.updateById(newBill);
        log.info("[红冲链] 新账单 {} 指向原账单 {}", newBill.getBillNo(), origBill.getBillNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizSettlementBill refreshFromTxns(Long billId) {
        BizSettlementBill bill = this.getById(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        BigDecimal charged = scale(nz(paymentTxnMapper.sumChargedByBill(billId)));
        BigDecimal refunded = scale(nz(paymentTxnMapper.sumRefundedByBill(billId)));
        BigDecimal net = scale(charged.subtract(refunded));
        BigDecimal payable = scale(nz(bill.getPayableAmount()));

        bill.setPaidAmount(charged);
        bill.setRefundAmount(refunded);
        if (!BillStatusEnum.VOIDED.getCode().equals(bill.getBillStatus())) {
            if (net.compareTo(payable) >= 0) {
                boolean justPaid = !BillStatusEnum.PAID.getCode().equals(bill.getBillStatus());
                if (justPaid) {
                    bill.setPayTime(LocalDateTime.now());
                }
                bill.setBillStatus(BillStatusEnum.PAID.getCode());
                // 钱真正收齐了，记账行才从「2-已锁定」提为「3-已结算」：
                // 这一步不在这里做，账单付清了费用行还挂着锁定，退费门禁（只认已结算）就把能退的钱挡在外面
                int settled = feeRecordService.markSettledByBill(billId);
                if (settled > 0) {
                    log.info("[结算] 账单 {} 付清，记账行提为已结算 {} 条", bill.getBillNo(), settled);
                }
                // 来源单据只在「转为已支付」这一刻推进一次：部分收款时处方不能先变成已缴费，
                // 否则发药窗口按方发药会把还没收够钱的药发出去
                if (justPaid) {
                    sourceAdvanceService.advanceByBill(bill);
                }
            } else if (net.signum() > 0) {
                bill.setBillStatus(BillStatusEnum.PARTIAL_PAID.getCode());
            } else if (refunded.signum() > 0) {
                // 收过的钱全退回去了：这张账单没有欠账，但也不能算"已支付"
                bill.setBillStatus(BillStatusEnum.REFUNDED.getCode());
            } else {
                bill.setBillStatus(BillStatusEnum.UNPAID.getCode());
            }
        }
        this.updateById(bill);
        return bill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizSettlementBillVO settleVO(BillSettleUpsertDTO dto) {
        return toVO(settle(dto));
    }

    @Override
    public PendingFeeVO pendingFees(Integer encounterType, Long encounterId) {
        if (EncounterTypeEnum.fromCode(encounterType) == null || encounterId == null) {
            throw new BusinessException("缺少就诊标识");
        }
        List<BizFeeRecord> rows = feeRecordService.listPending(encounterType, encounterId);
        List<BizFeeRecordVO> fees = new ArrayList<>(rows.size());
        BigDecimal total = BigDecimal.ZERO;
        for (BizFeeRecord row : rows) {
            BizFeeRecordVO vo = new BizFeeRecordVO();
            BeanUtils.copyProperties(row, vo);
            fees.add(vo);
            total = total.add(nz(row.getAmount()));
        }
        PendingFeeVO result = new PendingFeeVO();
        result.setFees(fees);
        result.setTotalAmount(total);
        result.setUnpaidBillAmount(unpaidAmount(encounterType, encounterId));
        return result;
    }

    @Override
    public BigDecimal unpaidAmount(Integer encounterType, Long encounterId) {
        if (encounterType == null || encounterId == null) {
            return BigDecimal.ZERO;
        }
        return scale(nz(baseMapper.sumUnpaidGap(encounterType, encounterId)));
    }

    @Override
    public List<BizSettlementBill> listByEncounter(Integer encounterType, Long encounterId) {
        return this.list(new LambdaQueryWrapper<BizSettlementBill>()
                .eq(BizSettlementBill::getEncounterType, encounterType)
                .eq(BizSettlementBill::getEncounterId, encounterId)
                .orderByDesc(BizSettlementBill::getId));
    }

    @Override
    public BizSettlementBill latestDischargeBill(Long admissionId) {
        if (admissionId == null) {
            return null;
        }
        BizSettlementBill latest = null;
        for (BizSettlementBill bill : listByEncounter(EncounterTypeEnum.INPATIENT.getCode(), admissionId)) {
            // 中途结算与门诊账单都不算出院结算；作废的那张是"这次结算撤销了、要重结"，也不算
            if (!BillTypeEnum.DISCHARGE.getCode().equals(bill.getBillType())
                    || BillStatusEnum.VOIDED.getCode().equals(bill.getBillStatus())) {
                continue;
            }
            if (latest == null || bill.getId() > latest.getId()) {
                latest = bill;
            }
        }
        return latest;
    }

    @Override
    public PageResult<BizSettlementBillVO> selectPage(BillQueryPageDTO query) {
        Page<BizSettlementBill> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()),
                buildWrapper(query));
        List<BizSettlementBillVO> records = new ArrayList<>();
        for (BizSettlementBill bill : page.getRecords()) {
            records.add(toVO(bill));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public PageResult<PendingEncounterVO> pendingEncounterPage(PendingEncounterQueryPageDTO query) {
        if (EncounterTypeEnum.fromCode(query.getEncounterType()) == null) {
            throw new BusinessException("就诊类型只能是 1-门诊 或 2-住院");
        }
        IPage<PendingEncounterVO> page = baseMapper.selectPendingEncounterPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public List<Map<String, Object>> pendingBillsForPatient(Long patientId) {
        if (patientId == null) {
            return List.of();
        }
        List<BizSettlementBill> bills = this.list(new LambdaQueryWrapper<BizSettlementBill>()
                .eq(BizSettlementBill::getPatientId, patientId)
                .in(BizSettlementBill::getBillStatus,
                        BillStatusEnum.UNPAID.getCode(), BillStatusEnum.PARTIAL_PAID.getCode())
                .orderByDesc(BizSettlementBill::getId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (BizSettlementBill bill : bills) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", String.valueOf(bill.getId()));
            m.put("billNo", bill.getBillNo());
            m.put("encounterNo", bill.getEncounterNo());
            m.put("payableAmount", bill.getPayableAmount());
            m.put("billTime", bill.getBillTime());
            m.put("billStatus", bill.getBillStatus());
            List<BizSettlementBillItem> items = billItemMapper.selectByBill(bill.getId());
            List<Map<String, Object>> details = items.stream().map(it -> {
                Map<String, Object> d = new LinkedHashMap<>();
                d.put("itemName", it.getItemName());
                d.put("amount", it.getAmount());
                d.put("deptName", it.getDeptName());
                d.put("specification", it.getSpecification());
                d.put("unit", it.getUnit());
                d.put("price", it.getPrice());
                d.put("quantity", it.getQuantity());
                // 医保拆分：患者端「自付为什么这么多」的自证依据，行级快照原样透出
                d.put("poolAmount", it.getPoolAmount());
                d.put("accountAmount", it.getAccountAmount());
                d.put("selfAmount", it.getSelfAmount());
                d.put("catalogType", it.getCatalogType());
                return d;
            }).toList();
            m.put("details", details);
            m.put("poolAmount", sumColumn(items, BizSettlementBillItem::getPoolAmount));
            m.put("accountAmount", sumColumn(items, BizSettlementBillItem::getAccountAmount));
            m.put("selfAmount", sumColumn(items, BizSettlementBillItem::getSelfAmount));
            result.add(m);
        }
        return result;
    }

    /**
     * 明细行某金额列求和（null 当 0）。患者端账单头的「医保报多少/自付多少」用它汇总。
     */
    private BigDecimal sumColumn(List<BizSettlementBillItem> items,
                                 Function<BizSettlementBillItem, BigDecimal> getter) {
        return items.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<BizSettlementBillItemVO> listItemsByPatient(Long patientId) {
        if (patientId == null) {
            return List.of();
        }
        List<BizSettlementBillItem> rows = billItemMapper.selectList(new LambdaQueryWrapper<BizSettlementBillItem>()
                .eq(BizSettlementBillItem::getPatientId, patientId)
                .orderByDesc(BizSettlementBillItem::getId));
        List<BizSettlementBillItemVO> vos = new ArrayList<>(rows.size());
        for (BizSettlementBillItem row : rows) {
            BizSettlementBillItemVO vo = new BizSettlementBillItemVO();
            BeanUtils.copyProperties(row, vo);
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public BizSettlementBillDetailVO getDetailById(Long billId) {
        BizSettlementBill bill = this.getById(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        BizSettlementBillDetailVO vo = new BizSettlementBillDetailVO();
        BeanUtils.copyProperties(bill, vo);
        vo.setUnpaidAmount(unpaidOf(bill));
        List<BizSettlementBillItem> items = billItemMapper.selectByBill(billId);
        List<BizPaymentTxn> txns = paymentTxnMapper.selectByBill(billId);
        vo.setItems(items);
        vo.setTxns(txns);
        return vo;
    }

    /**
     * 退费候选：账单下某一类（{@code refundType}）还能<b>整条</b>退的记账行，按出账顺序返回。
     *
     * <p>口径与 {@code PaymentServiceImpl.refund} 逐字一致，否则窗口说"能退"、收费处点执行才发现退不动：
     * 候选 = 本账单名下的正数行 - 已 4-已红冲的行 - 剩余净额 ≤ 0 的行，再按退费类型的项目类型圈范围。
     * 剩余净额要减掉名下<b>还没退场</b>的红冲负行（部分退过的行只剩一半可退）。
     */
    @Override
    public List<RefundableLineVO> refundableLines(Long billId, Integer refundType) {
        RefundTypeEnum type = RefundTypeEnum.getByCode(refundType);
        if (type == null) {
            throw new BusinessException("不支持的退费类型：" + refundType);
        }
        if (billId == null) {
            throw new BusinessException("缺少结算账单");
        }
        List<BizFeeRecord> rows = feeRecordService.listByBill(billId);
        if (CollectionUtils.isEmpty(rows)) {
            return new ArrayList<>();
        }
        Map<Long, BigDecimal> reversedByOrig = new HashMap<>();
        for (BizFeeRecord row : rows) {
            if (row.getOrigFeeId() != null) {
                reversedByOrig.merge(row.getOrigFeeId(), nz(row.getAmount()), BigDecimal::add);
            }
        }
        List<RefundableLineVO> result = new ArrayList<>();
        for (BizFeeRecord row : rows) {
            if (row.getOrigFeeId() != null
                    || nz(row.getAmount()).signum() <= 0
                    || FeeStatusEnum.REVERSED.getCode().equals(row.getFeeStatus())
                    || !type.covers(row.getItemType())) {
                continue;
            }
            BigDecimal remaining = scale(nz(row.getAmount()).add(nz(reversedByOrig.get(row.getId()))));
            if (remaining.signum() <= 0) {
                continue;
            }
            RefundableLineVO line = new RefundableLineVO();
            line.setFeeRecordId(row.getId());
            line.setItemType(row.getItemType());
            PaymentItemTypeEnum itemType = PaymentItemTypeEnum.getByCode(row.getItemType());
            line.setItemTypeText(itemType == null ? String.valueOf(row.getItemType()) : itemType.getDesc());
            line.setItemCode(row.getItemCode());
            line.setItemName(row.getItemName());
            line.setSpecification(row.getSpecification());
            line.setUnit(row.getUnit());
            line.setPrice(row.getPrice());
            line.setQuantity(row.getQuantity());
            line.setAmount(remaining);
            result.add(line);
        }
        return result;
    }

    /**
     * 取本次结算的记账行：给了ID按ID取（并校验确实属于该就诊、确实是待结算），
     * 没给则取该就诊全部待结算行（收费台「全部结算」）。
     */
    private List<BizFeeRecord> pickRows(BillSettleUpsertDTO dto) {
        List<BizFeeRecord> rows;
        if (CollectionUtils.isEmpty(dto.getFeeIds())) {
            rows = feeRecordService.listPending(dto.getEncounterType(), dto.getEncounterId());
        } else {
            rows = new ArrayList<>();
            for (BizFeeRecord row : feeRecordService.listByIds(dto.getFeeIds())) {
                if (!dto.getEncounterType().equals(row.getEncounterType())
                        || !dto.getEncounterId().equals(row.getEncounterId())) {
                    throw new BusinessException("记账行 " + row.getFeeNo() + " 不属于本次就诊");
                }
                rows.add(row);
            }
        }
        if (rows.isEmpty()) {
            throw new BusinessException("该就诊下没有待结算的费用");
        }
        Long patientId = rows.get(0).getPatientId();
        for (BizFeeRecord row : rows) {
            if (!java.util.Objects.equals(patientId, row.getPatientId())) {
                // 一张账单只能有一个付款人，跨患者合并结算在现金清点上是拆不开的
                throw new BusinessException("选中的费用不属于同一患者，不能合并结算");
            }
        }
        return rows;
    }

    /**
     * 账单行快照 + 逐行 split（按医保目录报销规则逐条算）。
     *
     * <p>split 必须落到行：2304 结算清单要逐项目报费率，整单一个数报不出去。
     */
    private List<BizSettlementBillItem> buildItems(BizFeeRecord first, List<BizFeeRecord> rows,
                                                   BigDecimal discount, PatientInsurance insurance) {
        // 批量查规则：避免 N+1
        List<String> itemCodes = new ArrayList<>();
        for (BizFeeRecord row : rows) {
            if (StringUtils.hasText(row.getItemCode())) {
                itemCodes.add(row.getItemCode());
            }
        }
        Map<String, BizInsuranceCatalogRule> ruleByItem = new HashMap<>();
        if (!itemCodes.isEmpty()) {
            LocalDate settleDate = LocalDate.now();
            List<BizInsuranceCatalogRule> rules = catalogRuleMapper.selectBatchRules(
                    itemCodes, first.getEncounterType(), insurance.type(), settleDate);
            for (BizInsuranceCatalogRule rule : rules) {
                // 同一项目可能有多条规则（不同 catalog_type），只取第一条（优先级最高）
                ruleByItem.putIfAbsent(rule.getItemCode(), rule);
            }
        }

        List<BizSettlementBillItem> items = new ArrayList<>();
        for (BizFeeRecord row : rows) {
            BizSettlementBillItem item = new BizSettlementBillItem();
            item.setFeeRecordId(row.getId());
            item.setPatientId(row.getPatientId());
            item.setEncounterType(row.getEncounterType());
            item.setEncounterId(row.getEncounterId());
            item.setDeptId(row.getDeptId());
            item.setDeptName(cut(row.getDeptName(), W_DEPT_NAME));
            item.setItemType(row.getItemType());
            item.setItemCode(cut(row.getItemCode(), W_ITEM_CODE));
            item.setItemName(cut(row.getItemName(), W_ITEM_NAME));
            item.setSpecification(cut(row.getSpecification(), W_SPEC));
            item.setUnit(cut(row.getUnit(), W_UNIT));
            item.setPrice(nz(row.getPrice()));
            item.setQuantity(nz(row.getQuantity()));
            item.setCatalogType(row.getCatalogType() == null ? CATALOG_SELF_PAY : row.getCatalogType());
            BigDecimal amount = scale(nz(row.getAmount()));
            item.setAmount(amount);
            item.setDiscountAmount(BigDecimal.ZERO);

            // 按规则表逐条算 pool/account/self
            BizInsuranceCatalogRule rule = StringUtils.hasText(row.getItemCode())
                    ? ruleByItem.get(row.getItemCode()) : null;
            BigDecimal pool = insurance.covered() ? computePoolFromRule(amount, item.getCatalogType(), rule, insurance) : BigDecimal.ZERO;
            item.setPoolAmount(pool);
            // 个账是"刷参保人卡扣的额度"，是一笔真实收款（pay_method=4），不是账单层的分摊：
            // 报盘没回这个数之前恒为 0，绝不拿估算值去充门面
            item.setAccountAmount(BigDecimal.ZERO);
            item.setSelfAmount(scale(amount.subtract(pool)));
            items.add(item);
        }
        allocateDiscount(items, discount);
        return items;
    }

    /**
     * 按规则表算统筹：优先用规则的 self_pay_ratio/pool_ratio，没有规则时退回到整单比例。
     */
    private BigDecimal computePoolFromRule(BigDecimal amount, Integer catalogType,
                                           BizInsuranceCatalogRule rule, PatientInsurance insurance) {
        if (amount.signum() <= 0 || catalogType == null) {
            return BigDecimal.ZERO;
        }
        // 有规则时按规则算
        if (rule != null) {
            // 先扣自付比例
            BigDecimal selfPayRatio = rule.getSelfPayRatio() == null ? BigDecimal.ZERO : rule.getSelfPayRatio();
            BigDecimal afterSelfPay = amount;
            if (catalogType == CATALOG_B && selfPayRatio.signum() > 0) {
                // 乙类先自付 X%
                BigDecimal firstSelf = rate(amount, selfPayRatio);
                afterSelfPay = amount.subtract(firstSelf);
            } else if (catalogType == CATALOG_C || catalogType == CATALOG_SELF_PAY) {
                // 丙类/自费全自费
                return BigDecimal.ZERO;
            }
            // 再套封顶线
            BigDecimal ceiling = rule.getCeiling();
            if (ceiling != null && ceiling.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(ceiling) > 0) {
                afterSelfPay = BigDecimal.ZERO; // 超过封顶线的部分全自费
            }
            // 最后按统筹比例报
            BigDecimal poolRatio = rule.getPoolRatio();
            if (poolRatio != null && poolRatio.signum() > 0) {
                return rate(afterSelfPay, poolRatio);
            }
            return BigDecimal.ZERO;
        }
        // 没有规则时退回到整单比例（向后兼容）
        return computePool(amount, catalogType, insurance);
    }

    /**
     * 甲类按报销比例进统筹；乙类先扣先行自付，剩余部分再按比例报；丙类/自费不报。
     */
    private BigDecimal computePool(BigDecimal amount, Integer catalogType, PatientInsurance insurance) {
        if (amount.signum() <= 0 || catalogType == null) {
            return BigDecimal.ZERO;
        }
        if (catalogType == CATALOG_A) {
            return rate(amount, insurance.coverageRatio());
        }
        if (catalogType == CATALOG_B) {
            BigDecimal firstSelf = rate(amount, insurance.selfPayRatio());
            return rate(amount.subtract(firstSelf), insurance.coverageRatio());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal rate(BigDecimal amount, BigDecimal percent) {
        if (percent == null || percent.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(percent).divide(HUNDRED, AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 优惠按正数行的金额比例分摊，最后一行倒挤，保证 Σ行优惠 == 账单优惠。
     *
     * <p>只摊在正数行上：负行是冲减，给它摊优惠等于把退款又打折一次。
     */
    private void allocateDiscount(List<BizSettlementBillItem> items, BigDecimal discount) {
        if (discount == null || discount.signum() == 0) {
            return;
        }
        BigDecimal positiveTotal = BigDecimal.ZERO;
        for (BizSettlementBillItem item : items) {
            if (item.getAmount().signum() > 0) {
                positiveTotal = positiveTotal.add(item.getAmount());
            }
        }
        if (positiveTotal.signum() <= 0) {
            throw new BusinessException("优惠无法分摊：本次结算没有正向应收行");
        }
        BigDecimal left = discount;
        List<BizSettlementBillItem> positives = new ArrayList<>();
        for (BizSettlementBillItem item : items) {
            if (item.getAmount().signum() > 0) {
                positives.add(item);
            }
        }
        for (int i = 0; i < positives.size(); i++) {
            BizSettlementBillItem item = positives.get(i);
            BigDecimal share = i == positives.size() - 1
                    ? left
                    : scale(item.getAmount().multiply(discount).divide(positiveTotal, AMOUNT_SCALE, RoundingMode.HALF_UP));
            if (share.compareTo(left) > 0) {
                share = left;
            }
            if (share.compareTo(item.getSelfAmount()) > 0) {
                share = item.getSelfAmount();
            }
            item.setDiscountAmount(share);
            item.setSelfAmount(scale(item.getAmount().subtract(item.getPoolAmount()).subtract(share)));
            left = scale(left.subtract(share));
        }
    }

    /**
     * 结算方式与医保口径：调用方没指定时按患者有没有医保号推。
     */
    private PatientInsurance resolveInsurance(Long patientId, Integer settlementMode) {
        PatientBriefVO patient = patientId == null ? null : patientGateway.findPatient(patientId);
        boolean hasInsurance = patient != null && StringUtils.hasText(patient.getMedicalInsuranceNo());
        SettlementModeEnum mode = SettlementModeEnum.getByCode(settlementMode);
        if (mode == null) {
            mode = hasInsurance ? SettlementModeEnum.INSURANCE : SettlementModeEnum.SELF_PAY;
        }
        if (mode != SettlementModeEnum.INSURANCE) {
            return new PatientInsurance(SettlementModeEnum.SELF_PAY, null, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        if (!hasInsurance) {
            throw new BusinessException("该患者没有医保号，不能按医保结算");
        }
        String insuranceType = patient.getMedicalInsuranceType();
        MedicalInsuranceTypeEnum typeEnum = MedicalInsuranceTypeEnum.parse(insuranceType);
        Integer settlementType = typeEnum == null ? null : typeEnum.getSettlementType();
        BigDecimal coverage = BigDecimal.ZERO;
        BigDecimal selfPay = BigDecimal.ZERO;
        if (settlementType != null && settlementType > 1) {
            coverage = nz(insurancePolicyService.getCoverageRatio(settlementType, insuranceType));
            if (coverage.signum() > 0) {
                selfPay = nz(insurancePolicyService.getSelfPayRatio(settlementType, insuranceType));
            }
        }
        return new PatientInsurance(SettlementModeEnum.INSURANCE, insuranceType, coverage, selfPay);
    }

    private BillTypeEnum resolveBillType(Integer billType, Integer encounterType) {
        BillTypeEnum type = BillTypeEnum.fromCode(billType);
        if (type != null) {
            return type;
        }
        return EncounterTypeEnum.INPATIENT.getCode().equals(encounterType)
                ? BillTypeEnum.INPATIENT_MID : BillTypeEnum.OUTPATIENT;
    }

    private LambdaQueryWrapper<BizSettlementBill> buildWrapper(BillQueryPageDTO query) {
        String keyword = query.getKeyword();
        return new LambdaQueryWrapper<BizSettlementBill>()
                .eq(query.getEncounterType() != null, BizSettlementBill::getEncounterType, query.getEncounterType())
                .eq(query.getEncounterId() != null, BizSettlementBill::getEncounterId, query.getEncounterId())
                .eq(query.getPatientId() != null, BizSettlementBill::getPatientId, query.getPatientId())
                .in(Boolean.TRUE.equals(query.getUnpaidOnly()),
                        BizSettlementBill::getBillStatus,
                        BillStatusEnum.UNPAID.getCode(), BillStatusEnum.PARTIAL_PAID.getCode())
                .in(query.getBillStatusList() != null && !query.getBillStatusList().isEmpty(),
                        BizSettlementBill::getBillStatus, query.getBillStatusList())
                .eq(query.getBillStatus() != null && (query.getBillStatusList() == null || query.getBillStatusList().isEmpty()),
                        BizSettlementBill::getBillStatus, query.getBillStatus())
                .eq(query.getBillType() != null, BizSettlementBill::getBillType, query.getBillType())
                .eq(query.getSettlementMode() != null, BizSettlementBill::getSettlementMode, query.getSettlementMode())
                .eq(query.getBillById() != null, BizSettlementBill::getBillById, query.getBillById())
                .ge(query.getBeginDate() != null, BizSettlementBill::getBillDate, query.getBeginDate())
                .le(query.getEndDate() != null, BizSettlementBill::getBillDate, query.getEndDate())
                .and(StringUtils.hasText(keyword), w -> w.like(BizSettlementBill::getBillNo, keyword)
                        .or().like(BizSettlementBill::getPatientName, keyword)
                        .or().like(BizSettlementBill::getEncounterNo, keyword))
                .orderByDesc(BizSettlementBill::getBillTime)
                .orderByDesc(BizSettlementBill::getId);
    }

    private BizSettlementBillVO toVO(BizSettlementBill bill) {
        BizSettlementBillVO vo = new BizSettlementBillVO();
        BeanUtils.copyProperties(bill, vo);
        vo.setUnpaidAmount(unpaidOf(bill));
        return vo;
    }

    /**
     * 尚需缴纳（净额口径）：unpaid = 应缴 − (已收 − 已退)。
     *
     * <p>三种状态没有"欠钱"这个概念，公式算出来只会说谎（全退的账单会显示"尚欠 ¥30"）：
     * 3-已支付收齐了、5-已退费钱全回去了、4-已作废的账单连应收都随着记账行解锁消失了 → 一律 0。
     */
    private BigDecimal unpaidOf(BizSettlementBill bill) {
        if (BillStatusEnum.PAID.getCode().equals(bill.getBillStatus())
                || BillStatusEnum.REFUNDED.getCode().equals(bill.getBillStatus())
                || BillStatusEnum.VOIDED.getCode().equals(bill.getBillStatus())) {
            return scale(BigDecimal.ZERO);
        }
        BigDecimal net = nz(bill.getPaidAmount()).subtract(nz(bill.getRefundAmount()));
        return scale(nz(bill.getPayableAmount()).subtract(net));
    }

    private BigDecimal sum(List<BizSettlementBillItem> items,
                           java.util.function.Function<BizSettlementBillItem, BigDecimal> getter) {
        BigDecimal total = BigDecimal.ZERO;
        for (BizSettlementBillItem item : items) {
            total = total.add(nz(getter.apply(item)));
        }
        return scale(total);
    }

    /**
     * 出账草稿：记账行 + 账单行快照 + 各类合计（不落库）。
     */
    private record Draft(List<BizFeeRecord> rows, List<BizSettlementBillItem> items, PatientInsurance insurance,
                         BigDecimal total, BigDecimal discount, BigDecimal pool, BigDecimal account,
                         BigDecimal self, BigDecimal payable) {
    }

    /**
     * 医保口径快照：结算方式 + 险种 + 报销比例 + 乙类先行自付比例（百分比数值，70 表示 70%）
     */
    private record PatientInsurance(SettlementModeEnum mode, String type,
                                    BigDecimal coverageRatio, BigDecimal selfPayRatio) {

        boolean covered() {
            return mode == SettlementModeEnum.INSURANCE;
        }
    }
}
