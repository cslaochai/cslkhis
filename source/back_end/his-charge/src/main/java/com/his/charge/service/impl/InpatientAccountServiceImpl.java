package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.charge.dto.*;
import com.his.charge.entity.BizAlert;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.mapper.BizAlertMapper;
import com.his.charge.mapper.BizPaymentTxnMapper;
import com.his.charge.service.FundAccountService;
import com.his.charge.service.InpatientAccountService;
import com.his.charge.service.PaymentService;
import com.his.charge.service.SettlementBillService;
import com.his.charge.support.InpatientAccountLabels;
import com.his.charge.vo.*;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.fee.entity.BizFeeRecord;
import com.his.fee.service.FeeRecordService;
import com.his.patient.entity.BizAdmission;
import com.his.patient.mapper.BizAdmissionMapper;
import com.his.patient.entity.BizPatient;
import com.his.patient.mapper.BizPatientMapper;
import com.his.system.enums.BizTypeEnum;
import com.his.system.entity.SysEmployee;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 住院账务服务实现（P3，四层口径）。
 *
 * <p>本类固化这些"至少会被追问一次"的点：
 *
 * <ol>
 *   <li><b>预交金是一段没有账单锚的资金流水</b>（sql/140）：充值/退款写进 L3
 *       支付资金流水（{@code bill_id IS NULL} + {@code source_type=3}，收正退负），
 *       额度同步进资金账户（主体=这次住院）。余额永远是账户那边 {@code SUM} 出来的，
 *       {@code balance_after} 只是抽查对账用的快照。旧表旧预交金那套自己的
 *       单号/余额/支付方式已停写 —— 同一件事实不许有两份记录。</li>
 *   <li><b>退款先判余额、再动渠道</b>：先发起渠道退款再回本地账，失败时就成了
 *       "钱退了、账没冲"的长款。而 FIFO 摊到具体原充值流水是必需的 ——
 *       微信收的钱只能退回微信（见 {@code PaymentService#prepayRefund}）。</li>
 *   <li><b>日清单读 L1 记账行</b>：正行与红冲负行一起出，合计天然等于应收净额；
 *       不再去旧收费明细上减退费金额（那是在第二张表里猜净额）。</li>
 *   <li><b>结算只有一套算法</b>：试算与出账共用 {@code SettlementBillService} 的同一份草稿，
 *       本类只加"账户里有多少钱"这一段（抵扣/退差/欠费）。住院结算出现第二套算法，
 *       小票就和结算单对不上。</li>
 *   <li><b>出院结算 = 一张 {@code bill_type=4} 的账单</b>：本类不再维护"结算台账"，
 *       {@code #settlementDetail} 与出院门禁读同一张账单，欠费额由应缴与已收现算。</li>
 *   <li><b>欠费不阻断诊疗</b>：{@code #summary} 只回答"欠不欠、欠多少"并留一条预警记录，
 *       真正的拦截只有一处在出院侧（没付清不让出院）—— 急救被欠费卡住是医疗事故，不是财务纪律。
 *       公式只有 {@code #arrearsView} 一处，欠费管控 gate 与概览共用它，
 *       各抄一份就会出现"前台说欠 800、医生开医嘱却说不欠"。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientAccountServiceImpl implements InpatientAccountService {

    /**
     * 金额统一两位小数（元）
     */
    private static final int SCALE = 2;

    /**
     * 流水类型（对外口径，与支付流水 direction 同码值：1-充值 2-退款）
     */
    private static final int PREPAY_IN = 1;
    private static final int PREPAY_OUT = 2;

    /**
     * 结算状态（派生值，库里没有这一列）
     */
    private static final int SETTLE_CLEARED = 1;
    private static final int SETTLE_ARREARS = 2;

    /**
     * 欠费告警类型（写入预警记录.alert_type）
     */
    private static final String ALERT_ARREARS = "ARREARS";

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter SECOND = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BizPaymentTxnMapper paymentTxnMapper;
    private final BizAlertMapper alertMapper;
    private final BizAdmissionMapper admissionMapper;
    private final BizPatientMapper patientMapper;
    private final PaymentService paymentService;
    private final FundAccountService fundAccountService;
    private final FeeRecordService feeRecordService;
    private final SettlementBillService settlementBillService;
    /**
     * 站内信（arrears 发送方）：欠费告警同步通知主管医生
     */
    private final SysMessageService sysMessageService;
    private final SysEmployeeMapper sysEmployeeMapper;

    // 预交金（L3 资金流水 + 住院资金账户）

    /**
     * 结算结论：把"应缴、账户、抵扣、退、欠"几段话说清。前端不许自己拼（拼出来就是第三套口径）。
     */
    private static String conclusion(BigDecimal payable, BigDecimal balance, BigDecimal used,
                                     BigDecimal refund, BigDecimal arrears) {
        String head = "应缴 " + payable.toPlainString() + " 元，住院账户 " + balance.toPlainString()
                + " 元（本次抵扣 " + used.toPlainString() + " 元）";
        if (arrears.signum() > 0) {
            return head + "，结算后欠费 " + arrears.toPlainString() + " 元，未付清不能办理出院";
        }
        if (refund.signum() > 0) {
            return head + "，结算后应退 " + refund.toPlainString() + " 元（已转入患者院内余额）";
        }
        return head + "，已结清";
    }

    private static boolean outOfRange(String day, String beginDate, String endDate) {
        if (StringUtils.hasText(beginDate) && day.compareTo(beginDate) < 0) {
            return true;
        }
        return StringUtils.hasText(endDate) && day.compareTo(endDate) > 0;
    }

    private static String dayOf(LocalDateTime time) {
        return time == null ? null : time.toLocalDate().format(DAY);
    }

    // 日清单（L1 记账行按天汇总）

    private static BigDecimal scale(BigDecimal value) {
        return nz(value).setScale(SCALE, RoundingMode.HALF_UP);
    }

    // 出院结算（L2 出账单 + L3 余额抵扣/退差）

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * DATETIME(0) 是四舍五入不是截断：落库又要比较的时间统一截到秒
     */
    private static LocalDateTime toSeconds(LocalDateTime time) {
        return time == null ? null : time.truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public IPage<PrepayVO> prepayListPage(PrepayQueryPageDTO query) {
        PrepayQueryPageDTO q = query != null ? query : new PrepayQueryPageDTO();
        Page<PrepayVO> page = new Page<>(q.getPageNum(), q.getPageSize());
        IPage<PrepayVO> raw = paymentTxnMapper.selectPrepayPage(page, q);
        // 文案由后端给：前端判码值就会有第二套口径（支付方式码值前端就抄错过一次，把 4 当银行卡）
        for (PrepayVO vo : raw.getRecords()) {
            vo.setPrepayTypeText(InpatientAccountLabels.prepayTypeText(vo.getPrepayType()));
            vo.setPayMethodText(InpatientAccountLabels.payMethodText(vo.getPayMethod()));
        }
        return raw;
    }

    // 账务概览（欠费提示）

    @Override
    public PrepayBalanceVO balance(Long admissionId) {
        BizAdmission admission = requireAdmission(admissionId);
        BizPatient patient = patientMapper.selectById(admission.getPatientId());
        PrepayBalanceVO vo = new PrepayBalanceVO();
        vo.setAdmissionId(admission.getAdmissionId());
        vo.setPatientName(patient != null ? patient.getPatientName() : null);
        vo.setRechargeTotal(scale(paymentTxnMapper.sumPrepayRecharge(admission.getAdmissionId())));
        vo.setRefundTotal(scale(paymentTxnMapper.sumPrepayRefunded(admission.getAdmissionId())));
        // 余额取资金账户：那才是"还能用、还能退多少"（已扣掉中途结算的余额抵扣与出院退差）。
        // 它不等于净预交流水，两者相等只发生在没抵扣过的住院上
        vo.setBalance(scale(fundAccountService.admissionBalance(admission.getAdmissionId())));
        vo.setFlowCount(Math.toIntExact(paymentTxnMapper.countPrepay(admission.getAdmissionId())));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PrepayVO> savePrepay(PrepayUpsertDTO dto) {
        if (dto == null || dto.getPrepayType() == null
                || (dto.getPrepayType() != PREPAY_IN && dto.getPrepayType() != PREPAY_OUT)) {
            throw new BusinessException("流水类型不合法（应为 1-充值 或 2-退款）");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于 0（退款金额传正数即可，方向由流水类型决定）");
        }
        BizAdmission admission = requireAdmission(dto.getAdmissionId());
        BizPatient patient = patientMapper.selectById(admission.getPatientId());
        PaymentService.PrepaySpec spec = prepaySpec(admission, patient, dto.getAmount(),
                dto.getPayMethod() == null ? PaymentMethodEnum.CASH.getCode() : dto.getPayMethod(),
                dto.getReceiptNo(), dto.getPrepayType() == PREPAY_IN ? dto.getChannelTxnNo() : null,
                dto.getPayTime(), dto.getRemark());

        // 钱的事实与账户额度在 L3 同一事务里落；本类只做"入院 → 患者身份"这一层翻译
        List<BizPaymentTxn> txns = dto.getPrepayType() == PREPAY_IN
                ? List.of(paymentService.prepayDeposit(spec))
                : paymentService.prepayRefund(spec);

        BigDecimal after = scale(fundAccountService.admissionBalance(admission.getAdmissionId()));
        List<PrepayVO> rows = new ArrayList<>();
        for (BizPaymentTxn txn : txns) {
            rows.add(toPrepayVO(txn, after));
        }
        log.info("住院预交金：入院ID={} 类型={} 金额={} 落库 {} 笔 余额={}",
                admission.getAdmissionId(), dto.getPrepayType(), dto.getAmount(), rows.size(), after);
        return rows;
    }

    // 私有方法

    @Override
    public DailyBillVO dailyBill(Long admissionId, String beginDate, String endDate) {
        BizAdmission admission = requireAdmission(admissionId);
        BizPatient patient = patientMapper.selectById(admission.getPatientId());
        List<BizFeeRecord> rows = feeRecordService.listNetByEncounter(
                EncounterTypeEnum.INPATIENT.getCode(), admission.getAdmissionId());

        // TreeMap：清单按天正序排。日清单表达的是"这次住院花了什么钱"，
        // 结算只是把账结掉，不该让清单在结算后变空（结算完查不到费用是最典型的一线投诉），
        // 所以这里读全部净额行、不按结算状态筛
        Map<String, List<DailyBillItemVO>> byDay = new TreeMap<>();
        BigDecimal total = BigDecimal.ZERO;
        int itemCount = 0;
        for (BizFeeRecord row : rows) {
            String day = dayOf(row.getBookTime());
            if (day == null || outOfRange(day, beginDate, endDate)) {
                continue;
            }
            DailyBillItemVO item = toItemVO(row);
            byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(item);
            total = total.add(item.getAmount());
            itemCount++;
        }

        List<DailyBillDayVO> days = new ArrayList<>();
        for (Map.Entry<String, List<DailyBillItemVO>> e : byDay.entrySet()) {
            DailyBillDayVO dayVO = new DailyBillDayVO();
            dayVO.setDate(e.getKey());
            dayVO.setItems(e.getValue());
            dayVO.setDayTotal(scale(e.getValue().stream()
                    .map(DailyBillItemVO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)));
            days.add(dayVO);
        }

        DailyBillVO vo = new DailyBillVO();
        vo.setAdmissionId(admission.getAdmissionId());
        vo.setAdmissionNo(admission.getAdmissionNo());
        vo.setPatientName(patient != null ? patient.getPatientName() : null);
        vo.setBeginDate(days.isEmpty() ? null : days.get(0).getDate());
        vo.setEndDate(days.isEmpty() ? null : days.get(days.size() - 1).getDate());
        vo.setTotalAmount(scale(total));
        vo.setItemCount(itemCount);
        vo.setDays(days);
        return vo;
    }

    @Override
    public InpatientSettlementPreviewVO preview(Long admissionId, Integer settleMode) {
        BizAdmission admission = requireAdmission(admissionId);
        BizPatient patient = patientMapper.selectById(admission.getPatientId());
        assertNotSettled(admission.getAdmissionId());

        BillPreviewVO draft = settlementBillService.previewSettlement(settleDraft(admission, settleMode));
        BigDecimal payable = scale(draft.getPayableAmount());
        BigDecimal balance = scale(fundAccountService.admissionBalance(admission.getAdmissionId()));
        BigDecimal used = payable.min(balance);
        BigDecimal refund = scale(balance.subtract(used));
        BigDecimal arrears = scale(payable.subtract(used));

        InpatientSettlementPreviewVO vo = new InpatientSettlementPreviewVO();
        vo.setAdmissionId(admission.getAdmissionId());
        vo.setPatientName(patient != null ? patient.getPatientName() : null);
        vo.setFeeCount(draft.getFeeCount());
        // 展示行就是草稿挑中的那批待结算记账行；钱一律以试算结果为准，这里不再自己加总一遍
        vo.setFeeRows(toItems(feeRecordService.listPending(
                EncounterTypeEnum.INPATIENT.getCode(), admission.getAdmissionId())));
        vo.setTotalAmount(scale(draft.getTotalAmount()));
        vo.setDiscountAmount(scale(draft.getDiscountAmount()));
        vo.setPoolAmount(scale(draft.getPoolAmount()));
        vo.setAccountAmount(scale(draft.getAccountAmount()));
        vo.setSelfAmount(scale(draft.getSelfAmount()));
        vo.setPayableAmount(payable);
        vo.setPrepayBalance(balance);
        vo.setBalanceUsed(scale(used));
        vo.setRefundAmount(refund);
        vo.setArrearsAmount(arrears);
        vo.setSettleMode(draft.getSettlementMode());
        vo.setSettleModeText(draft.getSettlementModeText());
        vo.setInsuranceType(draft.getInsuranceType());
        vo.setWillArrears(arrears.signum() > 0);
        vo.setConclusionText(conclusion(payable, balance, used, refund, arrears));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InpatientSettlementVO settle(InpatientSettlementUpsertDTO dto) {
        // 试算里已经做了"已结算不能再结"与"没有待结算费用"的校验，这里复用，不复制一份规则
        InpatientSettlementPreviewVO preview = preview(dto.getAdmissionId(), dto.getSettleMode());
        BizAdmission admission = requireAdmission(dto.getAdmissionId());
        BizPatient patient = patientMapper.selectById(admission.getPatientId());

        BizSettlementBill bill = settlementBillService.settle(settleDraft(admission, dto.getSettleMode()));
        BigDecimal payable = scale(nz(bill.getPayableAmount()));
        BigDecimal balance = scale(preview.getPrepayBalance());

        // 先用住院账户里的钱抵（一笔余额支付流水：钱从这次住院的账户进账单）。
        // 抵不完的部分留在账单上成为欠费 —— 出院门禁读的就是这个未付清差额
        BigDecimal used = payable.min(balance);
        if (used.signum() > 0) {
            paymentService.pay(balanceDeduction(bill, used));
        }
        // 抵完还剩的钱转进患者的院内余额账户：它离开这次住院但没离开医院，
        // 所以既不是收入也不该进收银员的点钞数（患者要取现，再从余额走一次柜面退款）
        BigDecimal refund = scale(balance.subtract(used));
        if (refund.signum() > 0) {
            paymentService.dischargeRemainder(prepaySpec(admission, patient, refund, null, null, null, null,
                    "出院结算退差（账单 " + bill.getBillNo() + "）"));
        }

        BizSettlementBill settled = settlementBillService.getById(bill.getId());
        BigDecimal arrears = scale(nz(settled.getPayableAmount()).subtract(nz(settled.getPaidAmount())));
        if (arrears.signum() > 0) {
            writeArrearsAlert(admission, patient, payable, balance, arrears);
        }

        log.info("住院结算：入院ID={} 账单={} 应收={} 统筹={} 应缴={} 抵扣={} 退差={} 欠费={}",
                admission.getAdmissionId(), settled.getBillNo(), settled.getTotalAmount(),
                settled.getPoolAmount(), payable, used, refund, arrears);
        return toSettlementVO(settled, used, refund, arrears);
    }

    @Override
    public InpatientSettlementVO settlementDetail(Long admissionId) {
        // C 类保留：入参是 Long（GET 参数直传），没有 DTO 承载注解；@RequestParam 已 required，此处是直调兜底
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        BizSettlementBill bill = settlementBillService.latestDischargeBill(admissionId);
        if (bill == null) {
            return null;
        }
        // 抵扣额按流水现算：账单 paid_amount 里可能还混着柜面补的现金，两个数不能互相代替
        BigDecimal used = scale(paymentTxnMapper.sumBalanceDeductByBill(bill.getId()));
        BigDecimal refund = scale(paymentTxnMapper.sumDischargeDiff(bill.getEncounterId()));
        BigDecimal arrears = scale(nz(bill.getPayableAmount()).subtract(nz(bill.getPaidAmount())));
        return toSettlementVO(bill, used, refund, arrears);
    }

    @Override
    public ArrearsView arrearsView(Long admissionId) {
        Integer inpatient = EncounterTypeEnum.INPATIENT.getCode();
        BigDecimal total = scale(feeRecordService.sumNetAmount(inpatient, admissionId));
        BigDecimal balance = scale(fundAccountService.admissionBalance(admissionId));
        // 已收 = 净预交（充值 − 柜面退款）+ 在账单上直接收的钱。后者必须排除余额抵扣：
        // 那笔钱就是预交金，再算一遍等于同一笔钱计两次，欠 800 的人会被显示成不欠
        BigDecimal collected = scale(nz(paymentTxnMapper.sumPrepayNet(admissionId))
                .add(nz(paymentTxnMapper.sumDirectChargedByEncounter(inpatient, admissionId))));
        return new ArrearsView(total, collected, balance, scale(total.subtract(collected).max(BigDecimal.ZERO)));
    }

    @Override
    public InpatientAccountSummaryVO summary(Long admissionId) {
        BizAdmission admission = requireAdmission(admissionId);
        BizPatient patient = patientMapper.selectById(admission.getPatientId());

        ArrearsView state = arrearsView(admission.getAdmissionId());
        BigDecimal total = state.chargedNet();
        BigDecimal balance = state.prepayBalance();
        BigDecimal arrearsAmount = state.arrearsAmount();
        boolean arrears = arrearsAmount.signum() > 0;

        BizSettlementBill discharge = settlementBillService.latestDischargeBill(admission.getAdmissionId());
        Integer settleStatus = discharge == null ? null
                : (nz(discharge.getPayableAmount()).subtract(nz(discharge.getPaidAmount())).signum() > 0
                ? SETTLE_ARREARS : SETTLE_CLEARED);

        InpatientAccountSummaryVO vo = new InpatientAccountSummaryVO();
        vo.setAdmissionId(admission.getAdmissionId());
        vo.setAdmissionNo(admission.getAdmissionNo());
        vo.setPatientName(patient != null ? patient.getPatientName() : null);
        vo.setPrepayBalance(balance);
        vo.setTotalAmount(total);
        vo.setArrears(arrears);
        vo.setArrearsAmount(arrearsAmount);
        vo.setSettled(discharge != null);
        vo.setSettlementNo(discharge != null ? discharge.getBillNo() : null);
        vo.setSettleStatusText(InpatientAccountLabels.settleStatusText(settleStatus));
        vo.setHintText(arrears
                ? "住院费用已发生 " + total.toPlainString() + " 元，已收 " + state.collected().toPlainString()
                + " 元（住院账户余额 " + balance.toPlainString() + " 元），欠费 "
                + arrearsAmount.toPlainString() + " 元（仅提示，不阻断诊疗，急救优先）"
                : null);

        // 欠费留痕：同一个入院一天只记一条，避免每次打开页面刷一串告警
        vo.setAlertWritten(arrears
                && writeArrearsAlertDaily(admission, patient, total, balance, arrearsAmount));
        return vo;
    }

    /**
     * 出院结算的 L2 入参：该就诊下全部待结算记账行、账单类型固定为出院结算。
     *
     * <p>试算与出账共用它，两边算出的应缴必然是同一个数（同一份草稿）。
     * 医保类型不在这里传：由 L2 按患者参保号推，两处各判一次就会漂。
     */
    private BillSettleUpsertDTO settleDraft(BizAdmission admission, Integer settleMode) {
        BillSettleUpsertDTO dto = new BillSettleUpsertDTO();
        dto.setEncounterType(EncounterTypeEnum.INPATIENT.getCode());
        dto.setEncounterId(admission.getAdmissionId());
        dto.setBillType(BillTypeEnum.DISCHARGE.getCode());
        dto.setSettlementMode(settleMode);
        dto.setRemark("出院结算");
        return dto;
    }

    /**
     * 已结算过的住院不许再结一次（要重结先作废原账单）：否则同一笔钱能结算两遍。
     */
    private void assertNotSettled(Long admissionId) {
        BizSettlementBill exist = settlementBillService.latestDischargeBill(admissionId);
        if (exist != null) {
            throw new BusinessException("该住院已办理出院结算（账单号 " + exist.getBillNo()
                    + "），如需重新结算请先作废原账单");
        }
    }

    /**
     * 出院结算的余额抵扣：一笔院内余额收款，账户主体是<b>这次住院</b>（不是患者）。
     *
     * <p>source_type=8-账户余额抵扣让它在班结/日结里与真金白银分开看 ——
     * 这笔钱早就进过抽屉了，再点一遍就多出预交金那一块。
     */
    private BillPayDTO balanceDeduction(BizSettlementBill bill, BigDecimal amount) {
        BillPayDTO.PayItem item = new BillPayDTO.PayItem();
        item.setPayMethod(PaymentMethodEnum.BALANCE.getCode());
        item.setAmount(amount);
        item.setOwnerId(bill.getEncounterId());
        item.setRemark("出院结算余额抵扣");

        BillPayDTO dto = new BillPayDTO();
        dto.setBillId(bill.getId());
        dto.setSourceType(TxnSourceEnum.ACCOUNT_BALANCE.getCode());
        dto.setItems(List.of(item));
        return dto;
    }

    /**
     * 预交金入参：L3 不认识"入院"这个临床概念，它只登记"这笔钱挂在哪个主体上"，
     * 所以患者身份必须由这里翻译成快照传下去。
     *
     * <p>{@code channelTxnNo} 只有充值侧会传（小程序回调带回渠道交易号）；退款的原路
     * 由 {@code PaymentService} 顺着原流水取，不接受调用方指定。
     */
    private PaymentService.PrepaySpec prepaySpec(BizAdmission admission, BizPatient patient, BigDecimal amount,
                                                 Integer payMethod, String receiptNo, String channelTxnNo,
                                                 LocalDateTime txnTime, String remark) {
        return new PaymentService.PrepaySpec(admission.getAdmissionId(), admission.getPatientId(),
                patient != null ? patient.getPatientNo() : null,
                patient != null ? patient.getPatientName() : null,
                scale(amount), payMethod, receiptNo, channelTxnNo, txnTime, remark);
    }

    private BizAdmission requireAdmission(Long admissionId) {
        // C 类保留：私有兜底被多个入口与内部流程共用，Bean Validation 覆盖不到这一层
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        BizAdmission admission = admissionMapper.selectById(admissionId);
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        return admission;
    }

    /**
     * 欠费告警（结算产生欠费时写，每次结算最多一条）
     */
    private void writeArrearsAlert(BizAdmission admission, BizPatient patient,
                                   BigDecimal payable, BigDecimal balance, BigDecimal arrears) {
        BizAlert alert = new BizAlert();
        alert.setAlertNo(nextAlertNo());
        alert.setAlertType(ALERT_ARREARS);
        alert.setAlertContent("住院结算欠费：" + (patient != null ? patient.getPatientName() : "患者")
                + "（住院号 " + admission.getAdmissionNo() + "）应缴 " + payable.toPlainString()
                + " 元，住院账户余额 " + balance.toPlainString() + " 元，欠费 " + arrears.toPlainString() + " 元");
        alert.setAlertStatus(0);
        alert.setNotifyUserId(admission.getAdmitDoctorId());
        alert.setNotifyTime(toSeconds(LocalDateTime.now()));
        alert.setRemark("admissionId=" + admission.getAdmissionId());
        alertMapper.insert(alert);
        notifyArrears(admission, patient, "出院结算", payable, balance, arrears);
    }

    /**
     * 欠费告警（每日一条：医生站/护士站每次打开都会查概览，不去重会刷屏）
     */
    private boolean writeArrearsAlertDaily(BizAdmission admission, BizPatient patient,
                                           BigDecimal total, BigDecimal balance, BigDecimal arrears) {
        LocalDateTime since = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long exists = alertMapper.countRecent(ALERT_ARREARS, "admissionId=" + admission.getAdmissionId(), since);
        if (exists > 0) {
            return false;
        }
        BizAlert alert = new BizAlert();
        alert.setAlertNo(nextAlertNo());
        alert.setAlertType(ALERT_ARREARS);
        alert.setAlertContent("住院欠费提醒：" + (patient != null ? patient.getPatientName() : "患者")
                + "（住院号 " + admission.getAdmissionNo() + "）已发生费用 " + total.toPlainString()
                + " 元，住院账户余额 " + balance.toPlainString() + " 元，欠费 " + arrears.toPlainString()
                + " 元（仅提示，不阻断诊疗）");
        alert.setAlertStatus(0);
        alert.setNotifyUserId(admission.getAdmitDoctorId());
        alert.setNotifyTime(toSeconds(LocalDateTime.now()));
        alert.setRemark("admissionId=" + admission.getAdmissionId());
        alertMapper.insert(alert);
        notifyArrears(admission, patient, "在院余额预警", total, balance, arrears);
        return true;
    }

    /**
     * arrears 发送方：欠费告警落库后 → 站内信同步通知主管医生（admission.admit_doctor_id）。
     *
     * <p>跟着预警记录的去重节奏走：调用点保证「结算欠费每次结算最多一条、
     * 在院预警每日最多一条」，站内信不再单独去重，避免两套节奏对不齐。
     * 通知型（handle_status=null）：欠费不阻断诊疗（本类铁律），医生知晓即可。
     * 发送失败只记日志，不影响结算/预警主流程。
     */
    private void notifyArrears(BizAdmission admission, BizPatient patient, String scene,
                               BigDecimal totalAmount, BigDecimal balance, BigDecimal arrears) {
        Long doctorId = admission.getAdmitDoctorId();
        if (doctorId == null) {
            return;
        }
        try {
            SysEmployee doctor = sysEmployeeMapper.selectById(doctorId);
            String doctorName = doctor != null && StringUtils.hasText(doctor.getEmpName())
                    ? doctor.getEmpName() : "站内用户";
            String patientName = patient != null && StringUtils.hasText(patient.getPatientName())
                    ? patient.getPatientName() : "患者";
            String content = String.format(
                    "患者 %s（住院号 %s）%s欠费 %s 元：应付 %s 元，住院账户余额 %s 元。欠费仅提示、不阻断诊疗，请关注催缴或补缴预交金。",
                    patientName, admission.getAdmissionNo(), scene,
                    arrears.toPlainString(), totalAmount.toPlainString(), balance.toPlainString());
            String payload = cn.hutool.json.JSONUtil.toJsonStr(new LinkedHashMap<String, Object>() {{
                put("patientName", patientName);
                put("admissionNo", admission.getAdmissionNo());
                put("arrears", arrears.toPlainString());
                put("balance", balance.toPlainString());
                put("scene", scene);
            }});
            sysMessageService.sendSystemMessage(doctorId, doctorName,
                    "欠费提醒：" + patientName, content,
                    BizTypeEnum.ARREARS.getType(), admission.getAdmissionId(), "warning", payload, null);
        } catch (Exception ex) {
            log.warn("[欠费提醒] 站内信发送失败 admissionNo={} admitDoctorId={}",
                    admission.getAdmissionNo(), doctorId, ex);
        }
    }

    private String nextAlertNo() {
        String prefix = "BJ" + LocalDate.now().format(NO_DATE);
        return prefix + String.format("%04d", alertMapper.countByAlertNoPrefix(prefix) + 1);
    }

    /**
     * 支付流水 → 预交金流水出参：单号就用流水编号（不再有第二套 YJ 单号），
     * 方向由 {@code direction} 换算成对外的 1/2。
     *
     * <p>{@code balanceAfter} 是<b>本次操作完成后</b>的账户余额：柜面退款按 FIFO 可能摊成多笔，
     * 逐笔中间快照要去配对的账户流水里捞，而列表页要的是"这批退完还剩多少"。
     */
    private PrepayVO toPrepayVO(BizPaymentTxn t, BigDecimal balanceAfter) {
        Integer prepayType = PayDirectionEnum.CHARGE.getCode().equals(t.getDirection()) ? PREPAY_IN : PREPAY_OUT;
        PrepayVO vo = new PrepayVO();
        vo.setId(t.getId());
        vo.setPrepayNo(t.getTxnNo());
        vo.setAdmissionId(t.getEncounterId());
        vo.setPatientId(t.getPatientId());
        vo.setPatientNo(t.getPatientNo());
        vo.setPatientName(t.getPatientName());
        vo.setPrepayType(prepayType);
        vo.setPrepayTypeText(InpatientAccountLabels.prepayTypeText(prepayType));
        vo.setAmount(t.getAmount());
        vo.setBalanceAfter(balanceAfter);
        vo.setPayMethod(t.getPayMethod());
        vo.setPayMethodText(InpatientAccountLabels.payMethodText(t.getPayMethod()));
        vo.setReceiptNo(t.getReceiptNo());
        vo.setPayTime(t.getTxnTime());
        vo.setOperatorId(t.getCashierId());
        vo.setOperatorName(t.getCashierName());
        vo.setRemark(t.getRemark());
        return vo;
    }

    private List<DailyBillItemVO> toItems(List<BizFeeRecord> rows) {
        List<DailyBillItemVO> items = new ArrayList<>();
        for (BizFeeRecord row : rows) {
            items.add(toItemVO(row));
        }
        return items;
    }

    private DailyBillItemVO toItemVO(BizFeeRecord row) {
        DailyBillItemVO item = new DailyBillItemVO();
        item.setFeeNo(row.getFeeNo());
        item.setItemType(row.getItemType());
        item.setItemTypeName(InpatientAccountLabels.itemTypeText(row.getItemType()));
        item.setItemCode(row.getItemCode());
        item.setItemName(row.getItemName());
        item.setSpecification(row.getSpecification());
        item.setUnit(row.getUnit());
        item.setQuantity(row.getQuantity());
        item.setPrice(row.getPrice());
        // 记账行金额本身就是净额：红冲另写一行负数、不改原行，所以这里不做任何减法
        item.setAmount(scale(row.getAmount()));
        item.setOccurTime(row.getBookTime() == null ? null : row.getBookTime().format(SECOND));
        item.setSourceNo(row.getSourceNo());
        return item;
    }

    /**
     * L2 出院账单 + 两笔资金事实 → 结算单出参：没有"结算单表"，每个数都能指回账单列或流水。
     */
    private InpatientSettlementVO toSettlementVO(BizSettlementBill bill, BigDecimal balanceUsed,
                                                 BigDecimal refund, BigDecimal arrears) {
        InpatientSettlementVO vo = new InpatientSettlementVO();
        vo.setId(bill.getId());
        vo.setSettlementNo(bill.getBillNo());
        vo.setAdmissionId(bill.getEncounterId());
        vo.setPatientId(bill.getPatientId());
        vo.setPatientNo(bill.getPatientNo());
        vo.setPatientName(bill.getPatientName());
        vo.setFeeCount(bill.getFeeCount());
        vo.setTotalAmount(scale(nz(bill.getTotalAmount())));
        vo.setDiscountAmount(scale(nz(bill.getDiscountAmount())));
        vo.setPoolAmount(scale(nz(bill.getPoolAmount())));
        vo.setAccountAmount(scale(nz(bill.getAccountAmount())));
        vo.setSelfAmount(scale(nz(bill.getSelfAmount())));
        vo.setPayableAmount(scale(nz(bill.getPayableAmount())));
        vo.setPaidAmount(scale(nz(bill.getPaidAmount())));
        vo.setBalanceUsed(scale(balanceUsed));
        vo.setRefundAmount(scale(refund));
        vo.setArrearsAmount(scale(arrears));
        vo.setSettleStatus(arrears.signum() > 0 ? SETTLE_ARREARS : SETTLE_CLEARED);
        vo.setSettleStatusText(InpatientAccountLabels.settleStatusText(vo.getSettleStatus()));
        vo.setSettleMode(bill.getSettlementMode());
        vo.setSettleModeText(InpatientAccountLabels.settleModeText(bill.getSettlementMode()));
        vo.setInsuranceType(bill.getInsuranceType());
        vo.setSettleTime(bill.getBillTime());
        vo.setSettleBy(bill.getBillById());
        vo.setSettleByName(bill.getBillByName());
        vo.setRemark(bill.getRemark());
        return vo;
    }
}
