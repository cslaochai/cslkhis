package com.his.charge.spi;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.dto.BillSettleUpsertDTO;
import com.his.spi.dto.BillPayDTO;
import com.his.spi.dto.FeeBookDTO;
import com.his.spi.dto.PrepayPageQueryDTO;
import com.his.spi.dto.PrepayUpsertDTO;
import com.his.spi.service.ChargeFacadeService;
import com.his.spi.vo.BillBriefVO;
import com.his.spi.vo.BillItemVO;
import com.his.spi.vo.BizSettlementBillVO;
import com.his.spi.vo.CancelCommandVO;
import com.his.spi.vo.FeeRecordVO;
import com.his.spi.vo.FeeTypeSumVO;
import com.his.spi.vo.InsuranceBriefVO;
import com.his.spi.vo.PendingBillVO;
import com.his.spi.vo.PrepayBalanceVO;
import com.his.spi.vo.PrepayVO;
import com.his.spi.vo.RegistChargeCommandVO;
import com.his.spi.vo.SettlementStateVO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.entity.BizInsuranceSettlement;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.entity.BizSettlementBillItem;
import com.his.common.enums.ClinicDiagnosisTierEnum;
import com.his.common.enums.InpatientSettleStatusEnum;
import com.his.charge.mapper.BizInsuranceSettlementMapper;
import com.his.charge.mapper.BizSettlementBillItemMapper;
import com.his.charge.mapper.BizSettlementBillMapper;
import com.his.charge.service.FeeRecordService;
import com.his.charge.service.FundAccountService;
import com.his.charge.service.InpatientAccountService;
import com.his.charge.service.InsurancePolicyService;
import com.his.charge.service.PaymentService;
import com.his.charge.service.SettlementBillService;
import com.his.charge.service.ArrearsControlGateService;
import com.his.spi.vo.ArrearsControlCheckResultVO;
import com.his.common.enums.*;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * ChargeApi 在 his-charge 侧的实现（四层模型口径）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeFacadeServiceImpl implements ChargeFacadeService {

    private final BizSettlementBillMapper bizSettlementBillMapper;

    private final FeeRecordService feeRecordService;

    private final SettlementBillService settlementBillService;

    private final PaymentService paymentService;

    private final FundAccountService fundAccountService;

    private final InpatientAccountService inpatientAccountService;

    private final ArrearsControlGateService arrearsControlGateService;

    private final InsurancePolicyService insurancePolicyService;

    private final BizSettlementBillItemMapper bizSettlementBillItemMapper;

    private final BizInsuranceSettlementMapper bizInsuranceSettlementMapper;

    // ---------- L1 记账 ----------

    @Override
    public FeeRecordVO bookFee(FeeBookDTO dto) {
        return toFeeVO(feeRecordService.book(dto));
    }

    @Override
    public List<FeeRecordVO> bookBatch(List<FeeBookDTO> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return feeRecordService.bookBatch(list).stream().map(ChargeFacadeServiceImpl::toFeeVO).toList();
    }

    @Override
    public FeeRecordVO findBookedBySource(Integer sourceType, Long sourceId, String itemCode) {
        return toFeeVO(feeRecordService.findBookedBySource(sourceType, sourceId, itemCode));
    }

    @Override
    public FeeRecordVO reverse(Long feeId, String reason) {
        return toFeeVO(feeRecordService.reverse(feeId, reason));
    }

    @Override
    public List<FeeTypeSumVO> sumNetGroupByItemType(Integer encounterType, Long encounterId) {
        return feeRecordService.sumNetGroupByItemType(encounterType, encounterId);
    }

    @Override
    public List<FeeTypeSumVO> sumPendingGroupByItemType(Integer encounterType, Long encounterId) {
        return feeRecordService.sumPendingGroupByItemType(encounterType, encounterId);
    }

    // ---------- 账单与收款 ----------

    @Override
    public Map<Long, BillBriefVO> mapBillsByIds(Collection<Long> billIds) {
        Map<Long, BillBriefVO> result = new HashMap<>();
        if (billIds == null || billIds.isEmpty()) {
            return result;
        }
        for (BizSettlementBill bill : bizSettlementBillMapper.selectBatchIds(billIds)) {
            result.put(bill.getId(), toBrief(bill));
        }
        return result;
    }

    @Override
    public BillBriefVO getBill(Long billId) {
        if (billId == null) {
            return null;
        }
        BizSettlementBill bill = bizSettlementBillMapper.selectById(billId);
        return bill == null ? null : toBrief(bill);
    }

    @Override
    public BigDecimal unpaidAmount(Long billId) {
        if (billId == null) {
            return null;
        }
        BizSettlementBill bill = bizSettlementBillMapper.selectById(billId);
        if (bill == null) {
            return null;
        }
        return NumUtil.scale(NumUtil.orZero(bill.getPayableAmount()).subtract(NumUtil.orZero(bill.getPaidAmount())), 2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payBill(BillPayDTO dto) {
        paymentService.pay(dto);
    }

    @Override
    public List<PendingBillVO> pendingBills(Long patientId) {
        return settlementBillService.pendingBillViews(patientId);
    }

    @Override
    public List<BillBriefVO> pendingBillsOfEncounter(Integer encounterType, Long encounterId) {
        if (encounterType == null || encounterId == null) {
            return Collections.emptyList();
        }
        List<BillBriefVO> result = new ArrayList<>();
        for (BizSettlementBill bill : settlementBillService.listByEncounter(encounterType, encounterId)) {
            if (!isPendingBillStatus(bill.getBillStatus())) {
                continue;
            }
            BillBriefVO vo = new BillBriefVO();
            vo.setBillId(bill.getId());
            vo.setBillNo(bill.getBillNo());
            vo.setBillStatus(bill.getBillStatus());
            vo.setPayableAmount(bill.getPayableAmount());
            vo.setPaidAmount(bill.getPaidAmount());
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillBriefVO createVisitBill(Integer encounterType, Long encounterId) {
        if (encounterType == null || encounterId == null) {
            return null;
        }
        // 幂等：已有待缴账单直接返回，不重复出账（回诊/重复结诊场景）
        List<BillBriefVO> existing = pendingBillsOfEncounter(encounterType, encounterId);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        // 找该就诊全部「1-待结算」L1 行；没有就说明本次无应收（全免/零费用），不出账
        List<BizFeeRecord> pendingRows = feeRecordService.listPending(encounterType, encounterId);
        if (pendingRows == null || pendingRows.isEmpty()) {
            log.info("[结诊出账] 就诊 type={} id={} 无待结算费用行，不生成账单", encounterType, encounterId);
            return null;
        }
        BillSettleUpsertDTO dto = new BillSettleUpsertDTO();
        dto.setEncounterType(encounterType);
        dto.setEncounterId(encounterId);
        // feeIds 留空：settle 内部按 encounterType+encounterId 捞全部待结算行
        dto.setBillType(BillTypeEnum.OUTPATIENT.getCode());
        // 医生结诊时按自费出待缴账单；患者有医保的，收费台结算时再做医保 split
        dto.setSettlementMode(1);
        dto.setRemark("医生结诊自动出账");
        BizSettlementBill bill = settlementBillService.settle(dto);
        log.info("[结诊出账] 账单 {} 患者 {} 行 {} 应缴 ¥{}",
                bill.getBillNo(), bill.getPatientName(), bill.getFeeCount(),
                bill.getPayableAmount().toPlainString());
        return getBill(bill.getId());
    }

    /**
     * 待缴口径：1-待支付 2-部分支付 6-挂账欠费（与 settle 挂账口径一致）；结清/作废/退费不参与出码。
     */
    private boolean isPendingBillStatus(Integer billStatus) {
        return Objects.equals(billStatus, BillStatusEnum.UNPAID.getCode())
                || Objects.equals(billStatus, BillStatusEnum.PARTIAL_PAID.getCode())
                || Objects.equals(billStatus, 6);
    }

    // ---------- 账单读侧（费用解释 / 报表） ----------

    @Override
    public BizSettlementBillVO getBillDetail(Long billId) {
        if (billId == null) {
            return null;
        }
        BizSettlementBill bill = bizSettlementBillMapper.selectById(billId);
        if (bill == null) {
            return null;
        }
        BizSettlementBillVO vo = new BizSettlementBillVO();
        BeanUtils.copyProperties(bill, vo);
        return vo;
    }

    @Override
    public List<BillItemVO> listBillItems(Long billId) {
        if (billId == null) {
            return List.of();
        }
        List<BizSettlementBillItem> items = bizSettlementBillItemMapper.selectList(
                new LambdaQueryWrapper<BizSettlementBillItem>()
                        .eq(BizSettlementBillItem::getBillId, billId));
        List<BillItemVO> result = new ArrayList<>(items.size());
        for (BizSettlementBillItem item : items) {
            BillItemVO vo = new BillItemVO();
            BeanUtils.copyProperties(item, vo);
            result.add(vo);
        }
        return result;
    }

    @Override
    public InsuranceBriefVO latestInsurance(Long billId) {
        if (billId == null) {
            return null;
        }
        List<BizInsuranceSettlement> list = bizInsuranceSettlementMapper.selectList(
                new LambdaQueryWrapper<BizInsuranceSettlement>()
                        .eq(BizInsuranceSettlement::getBillId, billId)
                        .orderByDesc(BizInsuranceSettlement::getId)
                        .last("LIMIT 1"));
        if (list == null || list.isEmpty()) {
            return null;
        }
        InsuranceBriefVO vo = new InsuranceBriefVO();
        BeanUtils.copyProperties(list.get(0), vo);
        return vo;
    }

    @Override
    public List<BizSettlementBillVO> paidBills(String startDate, String endDate) {
        // 四层口径：已支付（bill_status=3）的结算账单，按账务归属日 bill_date 聚合
        List<BizSettlementBill> bills = bizSettlementBillMapper.selectList(
                new LambdaQueryWrapper<BizSettlementBill>()
                        .ge(startDate != null, BizSettlementBill::getBillDate, startDate)
                        .le(endDate != null, BizSettlementBill::getBillDate, endDate)
                        .eq(BizSettlementBill::getBillStatus, BillStatusEnum.PAID.getCode()));
        List<BizSettlementBillVO> result = new ArrayList<>(bills.size());
        for (BizSettlementBill bill : bills) {
            BizSettlementBillVO vo = new BizSettlementBillVO();
            BeanUtils.copyProperties(bill, vo);
            result.add(vo);
        }
        return result;
    }

    // ---------- 住院预交金 ----------

    @Override
    public void savePrepay(PrepayUpsertDTO dto) {
        inpatientAccountService.savePrepay(dto);
    }

    @Override
    public PrepayBalanceVO prepayBalance(Long admissionId) {
        return inpatientAccountService.balance(admissionId);
    }

    @Override
    public IPage<PrepayVO> prepayPage(PrepayPageQueryDTO query) {
        return inpatientAccountService.prepayListPage(query);
    }

    // ---------- 挂号费联动 ----------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillBriefVO createRegistCharge(RegistChargeCommandVO command) {
        if (command == null || command.getRegistId() == null) {
            return null;
        }
        if (command.isWaived()) {
            log.info("[挂号费] 免收，不记账不出账：registNo={} 依据={}",
                    command.getRegistNo(), command.getWaiveReason());
            return null;
        }

        // 主项目 + 加收项 双行（湘医保发〔2025〕58号价表结构：主项门诊诊查费 + 职级加收子码）：
        //   主项门诊诊查费(普通门诊) 011102020010000 = baseFee；
        //   专家档在主项之上加收对应子码(001/002/003) = surchargeFee，两行同 itemType=1、丙类。
        //   普通档 surchargeFee=0，只出主项一行；专家档出主项 + 加收两行。
        //   金额合计 = baseFee + surchargeFee = diagnosisFee（儿童总价），与历史单行口径完全一致，
        //   不破坏任何按账单总额判定的逻辑（退号、改约比价、医保 split、日结）。
        //
        // 🔑 为什么不是「挂号费 + 诊查费」两笔：58号已废止挂号费，门诊唯一费用是门诊诊查费；
        //   专家溢价是诊查费的「加收项」而非独立费用，故拆 base + 加收而非凭空捏第二笔。
        BigDecimal baseFee = NumUtil.orZero(command.getBaseFee());
        BigDecimal surchargeFee = NumUtil.orZero(command.getSurchargeFee());
        List<FeeBookDTO> books = new ArrayList<>(2);
        if (baseFee.signum() > 0) {
            // 诊查费按丙类：湘医保发〔2025〕58号 支付分类=丙类，医保不报、100% 自付，不进职工门诊统筹。
            // 丙类才能表达全自费（规则表 ICR202610180001：self_pay_ratio=100 / pool_ratio=0）
            books.add(buildBook(command, "门诊诊查费(普通门诊)", "ZCF", baseFee,
                    FeeCatalogTypeEnum.CLASS_C.getCode()));
        }
        if (surchargeFee.signum() > 0) {
            SurchargeMeta meta = surchargeMeta(command.getDiagnosisTier());
            books.add(buildBook(command, meta.name, meta.code, surchargeFee,
                    FeeCatalogTypeEnum.CLASS_C.getCode()));
        }
        if (books.isEmpty()) {
            // 诊查费为 0 却按"未免收"进来：零应收，处理方式与免收一致，别硬出空账单
            return null;
        }
        List<BizFeeRecord> rows = feeRecordService.bookBatch(books);
        List<Long> feeIds = new ArrayList<>(rows.size());
        for (BizFeeRecord row : rows) {
            feeIds.add(row.getId());
        }
        BillSettleUpsertDTO settleDTO = new BillSettleUpsertDTO();
        settleDTO.setEncounterType(EncounterTypeEnum.OUTPATIENT.getCode());
        settleDTO.setEncounterId(command.getRegistId());
        settleDTO.setFeeIds(feeIds);
        settleDTO.setBillType(BillTypeEnum.REGISTRATION.getCode());
        // 本单结算方式（1-全自费 2-医保联网）由挂号单透传，允许医保患者本单改自费
        settleDTO.setSettlementMode(command.getSettlementMode());
        // 部分免收（只免挂号费、诊查费照收）时账单仍要缴，但备注里必须留「免了哪一项、依据哪条策略」
        settleDTO.setRemark(TextUtil.hasText(command.getWaiveReason()) ? command.getWaiveReason() : null);
        BizSettlementBill bill = settlementBillService.settle(settleDTO);
        log.info("[挂号费] 记账出账完成 registNo={} 账单={} 应缴 ¥{}",
                command.getRegistNo(), bill.getBillNo(), bill.getPayableAmount().toPlainString());
        return toBrief(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRegistCharge(CancelCommandVO command) {
        if (command == null || command.getBillId() == null) {
            return false;
        }
        boolean closed = paymentService.closeBill(command.getBillId(),
                TextUtil.hasText(command.getReason()) ? command.getReason() : "退号",
                TxnSourceEnum.CANCEL_REGIST.getCode());
        if (closed) {
            log.info("[挂号费] 退号联动整单撤销：billId={} 操作人={}",
                    command.getBillId(), command.getOperator());
        }
        return closed;
    }

    // ---------- 挂号试算（收费预估） ----------

    @Override
    public BigDecimal getCoverageRatio(Integer settlementType, Integer medicalInsuranceType) {
        // 内部政策服务的险种键为费别名称(String)，端口按费别编码(Integer)入参：
        // 编码归一回标准费别名称后委托原服务，保留「精确匹配 → 按结算方式兜底、无配置返回 0」原逻辑。
        String insuranceTypeName = resolveInsuranceTypeName(medicalInsuranceType);
        return insurancePolicyService.getCoverageRatio(settlementType, insuranceTypeName);
    }

    // ---------- 住院结算状态 ----------

    @Override
    public SettlementStateVO state(Long admissionId) {
        SettlementStateVO state = new SettlementStateVO();
        if (admissionId == null) {
            state.setText("入院ID为空，无法校验结算状态");
            return state;
        }

        BizSettlementBill discharge = settlementBillService.latestDischargeBill(admissionId);
        BigDecimal balance = NumUtil.orZero(fundAccountService.admissionBalance(admissionId));
        if (discharge == null) {
            // 未结算也要把预交金余额带回去：前台靠这句话判断"是没钱结、还是压根没办结算"
            state.setSettled(false);
            state.setArrearsAmount(BigDecimal.ZERO);
            state.setText("尚未办理住院结算（预交金余额 " + balance.toPlainString() + " 元）");
            return state;
        }

        boolean paidOff = isPaidOff(discharge);
        state.setSettled(true);
        state.setSettlementNo(discharge.getBillNo());
        state.setArrearsAmount(paidOff ? BigDecimal.ZERO : NumUtil.orZero(discharge.getPayableAmount()).subtract(NumUtil.orZero(discharge.getPaidAmount())));
        // settle_status 是旧表字段语义，四层后由账单状态推导：1-已结清 2-欠费
        state.setSettleStatus(paidOff ? InpatientSettleStatusEnum.CLEARED.getCode() : InpatientSettleStatusEnum.ARREARS.getCode());
        state.setText("已结算（" + (paidOff ? "已结清" : "欠费 " + state.getArrearsAmount().toPlainString() + " 元")
                + "，账单号 " + discharge.getBillNo() + "，" + BillStatusEnum.getLabel(discharge.getBillStatus())
                + "，预交金余额 " + balance.toPlainString() + " 元）");
        return state;
    }

    @Override
    public ArrearsControlCheckResultVO checkNewOrderArrears(Long admissionId, Collection<Integer> orderClasses) {
        if (admissionId == null || orderClasses == null || orderClasses.isEmpty()) {
            return null;
        }
        ArrearsControlGateService.CheckResult check = arrearsControlGateService.checkNewOrder(
                new ArrearsControlGateService.OrderCheck(admissionId, new HashSet<>(orderClasses)));
        if (check == null || check.isAllowed()) {
            return null;
        }
        ArrearsControlCheckResultVO vo = new ArrearsControlCheckResultVO();
        vo.setAllowed(false);
        vo.setReason(check.getReason());
        return vo;
    }

    // ---------- 内部工具 ----------

    /**
     * 费别编码（MedicalInsuranceTypeEnum.settlementType）反查标准费别名称，供内部政策服务精确匹配；
     * 为空或识别不出返回 {@code null}，交由内部服务按结算方式兜底匹配。
     */
    private static String resolveInsuranceTypeName(Integer medicalInsuranceType) {
        if (medicalInsuranceType == null) {
            return null;
        }
        for (MedicalInsuranceTypeEnum e : MedicalInsuranceTypeEnum.values()) {
            if (medicalInsuranceType.equals(e.getSettlementType())) {
                return e.getName();
            }
        }
        return null;
    }

    /**
     * 是否已付清：应缴为 0 也算付清（预交金全额抵扣后账单应缴就是 0，没有流水但确实结清了）。
     */
    private boolean isPaidOff(BizSettlementBill bill) {
        return NumUtil.orZero(bill.getPayableAmount()).subtract(NumUtil.orZero(bill.getPaidAmount())).compareTo(BigDecimal.ZERO) <= 0;
    }

    private FeeBookDTO buildBook(RegistChargeCommandVO command, String itemName, String itemCode,
                                 BigDecimal amount, int catalogType) {
        FeeBookDTO dto = new FeeBookDTO();
        dto.setPatientId(command.getPatientId());
        dto.setPatientNo(command.getPatientNo());
        dto.setPatientName(command.getPatientName());
        dto.setEncounterType(EncounterTypeEnum.OUTPATIENT.getCode());
        dto.setEncounterId(command.getRegistId());
        dto.setEncounterNo(command.getRegistNo());
        dto.setDeptId(command.getDeptId());
        dto.setDeptName(command.getDeptName());
        dto.setDoctorId(command.getDoctorId());
        dto.setDoctorName(command.getDoctorName());
        dto.setItemType(PaymentItemTypeEnum.REGISTRATION_FEE.getCode());
        dto.setItemCode(itemCode);
        dto.setItemName(itemName);
        dto.setUnit("次");
        dto.setPrice(amount);
        dto.setQuantity(BigDecimal.ONE);
        dto.setSourceType(FeeSourceTypeEnum.REGISTRATION.getCode());
        dto.setSourceId(command.getRegistId());
        dto.setSourceNo(command.getRegistNo());
        dto.setCatalogType(catalogType);
        return dto;
    }

    /**
     * 加收项院内码与名称（按职级档位映射）。
     * 普通档(1/null)不加收，本方法只在 surchargeFee > 0 时调用，对应档位 2/3/4。
     * 院内码沿用主项前缀 ZCF + 加收标记 + 档位码，国家医保编码见 ClinicDiagnosisTierEnum.getYbCode()。
     */
    private static SurchargeMeta surchargeMeta(Integer tier) {
        ClinicDiagnosisTierEnum e = ClinicDiagnosisTierEnum.ofCode(tier);
        if (e == null || e == ClinicDiagnosisTierEnum.GENERAL) {
            // 兜底：正常不会走到这里（surchargeFee>0 已拦截普通档），给出可识别的回落值而非 NPE
            return new SurchargeMeta("ZCF_ADD_0", "门诊诊查费加收");
        }
        return new SurchargeMeta("ZCF_ADD_" + e.getCode(), "门诊诊查费加收(" + e.getLabel() + ")");
    }

    private BillBriefVO toBrief(BizSettlementBill bill) {
        BillBriefVO brief = new BillBriefVO();
        brief.setBillId(bill.getId());
        brief.setBillNo(bill.getBillNo());
        brief.setBillStatus(bill.getBillStatus());
        brief.setPayableAmount(bill.getPayableAmount());
        BigDecimal paid = NumUtil.orZero(bill.getPaidAmount()).subtract(NumUtil.orZero(bill.getRefundAmount()));
        brief.setPaidAmount(paid.signum() < 0 ? BigDecimal.ZERO : paid);
        return brief;
    }

    private static FeeRecordVO toFeeVO(BizFeeRecord row) {
        if (row == null) {
            return null;
        }
        FeeRecordVO vo = new FeeRecordVO();
        vo.setId(row.getId());
        vo.setFeeNo(row.getFeeNo());
        vo.setAmount(row.getAmount());
        return vo;
    }

    /** 加收项载体：院内码 + 展示名称 */
    private static final class SurchargeMeta {
        private final String code;
        private final String name;

        private SurchargeMeta(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
}
