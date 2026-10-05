package com.his.report.support;

import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.PaymentMethodEnum;
import com.his.report.enums.CdrArchiveStatusEnum;
import com.his.report.enums.CdrAdmitWayEnum;
import com.his.report.enums.CdrChargeStatusEnum;
import com.his.report.enums.CdrChargeTypeEnum;
import com.his.report.enums.CdrConsultStatusEnum;
import com.his.report.enums.CdrConsultTypeEnum;
import com.his.report.enums.CdrDiagTypeEnum;
import com.his.report.enums.CdrCriticalTypeEnum;
import com.his.report.enums.CdrCriticalValueStatusEnum;
import com.his.report.enums.CdrDischargeStatusEnum;
import com.his.report.enums.CdrEmergencyStatusEnum;
import com.his.report.enums.CdrFollowupStatusEnum;
import com.his.report.enums.CdrInpatientRecordStatusEnum;
import com.his.report.enums.CdrInpatientRecordTypeEnum;
import com.his.report.enums.CdrInpatientSettleStatusEnum;
import com.his.report.enums.CdrInspApplyStatusEnum;
import com.his.report.enums.CdrInspRecordStatusEnum;
import com.his.report.enums.CdrInsuranceSettleStatusEnum;
import com.his.report.enums.CdrLabApplyStatusEnum;
import com.his.report.enums.CdrLabRecordStatusEnum;
import com.his.report.enums.CdrNursingTypeEnum;
import com.his.report.enums.CdrOperationStatusEnum;
import com.his.report.enums.CdrOrderStatusEnum;
import com.his.report.enums.CdrOrderTypeEnum;
import com.his.report.enums.CdrOutpatientRecordStatusEnum;
import com.his.report.enums.CdrPrescriptionStatusEnum;
import com.his.report.enums.CdrPrescriptionTypeEnum;
import com.his.report.enums.CdrPublicHealthReportStatusEnum;
import com.his.report.enums.CdrPublicHealthReportTypeEnum;
import com.his.report.enums.CdrQcStatusEnum;
import com.his.report.enums.CdrQueueStatusEnum;
import com.his.report.enums.CdrReferralStatusEnum;
import com.his.report.enums.CdrRegistStatusEnum;
import com.his.report.enums.CdrSummaryStatusEnum;
import com.his.report.enums.CdrTransferStatusEnum;
import com.his.report.enums.CdrTransfusionStatusEnum;
import com.his.report.enums.CdrTreatmentStatusEnum;

import java.util.function.Function;

/**
 * CDR 时间轴上的**事件类型**表。
 *
 * <p>一个枚举 = 一类临床事件，同时声明：中文名、归属的就诊形态、状态码怎么翻译、
 * 以及"副码"（文书类型 / 医嘱类别 / 诊断类型…）怎么翻译。
 *
 * <p>为什么码值翻译不写在 SQL 里：写进 SQL 的 CASE 没人维护，码值一变就静默错。
 * 翻译一律引用各枚举的 {@code getText}（展示口径：null / 脏码值 → 空串），
 * 唯一出口在枚举，这里只做注册表，不承载映射。
 *
 * <p>归类规律：门诊事件挂挂号（REGIST/visit），住院事件挂入院（ADMISSION），
 * 危急值/质控/随访这类**跨就诊**的事挂患者（PATIENT）。
 */
public enum CdrEventType {

    // 门诊域
    REGIST("regist", "挂号", CdrNodeType.OUTPATIENT, CdrRegistStatusEnum::getText, null, null),

    OUTPATIENT_RECORD("outpatientRecord", "门诊病历", CdrNodeType.OUTPATIENT,
            CdrOutpatientRecordStatusEnum::getText, null, null),

    PRESCRIPTION("prescription", "处方", CdrNodeType.OUTPATIENT, CdrPrescriptionStatusEnum::getText,
            CdrPrescriptionTypeEnum::getText, "处方类型"),

    LAB_APPLY("laboratoryApply", "检验申请", CdrNodeType.OUTPATIENT,
            CdrLabApplyStatusEnum::getText, null, null),

    LAB_REPORT("laboratoryReport", "检验报告", CdrNodeType.OUTPATIENT,
            CdrLabRecordStatusEnum::getText, null, null),

    INSP_APPLY("inspectionApply", "检查申请", CdrNodeType.OUTPATIENT,
            CdrInspApplyStatusEnum::getText, null, null),

    INSP_REPORT("inspectionReport", "检查报告", CdrNodeType.OUTPATIENT,
            CdrInspRecordStatusEnum::getText, null, null),

    TREATMENT("treatmentApply", "治疗单", CdrNodeType.OUTPATIENT,
            CdrTreatmentStatusEnum::getText, null, null),

    CHARGE("charge", "收费", CdrNodeType.OUTPATIENT, CdrChargeStatusEnum::getText,
            CdrChargeTypeEnum::getText, "收费类型"),

    INSURANCE_SETTLE("insuranceSettlement", "医保结算", CdrNodeType.OUTPATIENT,
            CdrInsuranceSettleStatusEnum::getText, null, null),

    QUEUE("queue", "候诊叫号", CdrNodeType.OUTPATIENT, CdrQueueStatusEnum::getText, null, null),

    RECORD_ARCHIVE("recordArchive", "病案归档", CdrNodeType.OUTPATIENT,
            CdrArchiveStatusEnum::getText, null, null),

    REPORT_DOC("report", "报告单", CdrNodeType.OUTPATIENT, null, null, null),

    // 住院域
    ADMISSION("admission", "入院登记", CdrNodeType.INPATIENT, AdmitStatusEnum::getText,
            CdrAdmitWayEnum::getText, "入院途径"),

    INPATIENT_RECORD("inpatientRecord", "住院文书", CdrNodeType.INPATIENT,
            CdrInpatientRecordStatusEnum::getText, CdrInpatientRecordTypeEnum::getText, "文书类型"),

    INPATIENT_ORDER("inpatientOrder", "住院医嘱", CdrNodeType.INPATIENT,
            CdrOrderStatusEnum::getText, CdrOrderTypeEnum::getText, "医嘱类型"),

    INPATIENT_DIAGNOSIS("inpatientDiagnosis", "住院诊断", CdrNodeType.INPATIENT,
            null, CdrDiagTypeEnum::getText, "诊断类型"),

    INPATIENT_SUMMARY("inpatientSummary", "病案首页", CdrNodeType.INPATIENT,
            CdrSummaryStatusEnum::getText, null, null),

    OPERATION_APPLY("operationApply", "手术申请", CdrNodeType.INPATIENT,
            CdrOperationStatusEnum::getText, null, null),

    INPATIENT_OPERATION("inpatientOperation", "手术记录", CdrNodeType.INPATIENT,
            null, null, null),

    CONSULTATION("consultation", "会诊", CdrNodeType.INPATIENT,
            CdrConsultStatusEnum::getText, CdrConsultTypeEnum::getText, "会诊范围"),

    TRANSFER("transfer", "转科", CdrNodeType.INPATIENT, CdrTransferStatusEnum::getText, null, null),

    TRANSFUSION("transfusion", "输血", CdrNodeType.INPATIENT,
            CdrTransfusionStatusEnum::getText, null, null),

    NURSING_RECORD("nursingRecord", "护理记录", CdrNodeType.INPATIENT,
            CdrInpatientRecordStatusEnum::getText, CdrNursingTypeEnum::getText, "文书类型"),

    PREPAY("prepay", "预交金", CdrNodeType.INPATIENT, null, PaymentMethodEnum::getText, "支付方式"),

    DISCHARGE("discharge", "出院", CdrNodeType.INPATIENT, CdrDischargeStatusEnum::getText, null, null),

    INPATIENT_SETTLE("inpatientSettlement", "住院结算", CdrNodeType.INPATIENT,
            CdrInpatientSettleStatusEnum::getText, null, null),

    // 患者级（跨就诊）
    EMERGENCY("emergency", "急诊", CdrNodeType.EMERGENCY, CdrEmergencyStatusEnum::getText, null, null),

    CRITICAL_VALUE("criticalValue", "危急值", CdrNodeType.PATIENT, CdrCriticalValueStatusEnum::getText,
            CdrCriticalTypeEnum::getText, "偏离方向"),

    QC("qualityControl", "病案质控", CdrNodeType.PATIENT, CdrQcStatusEnum::getText, null, null),

    FOLLOWUP("followupTask", "随访", CdrNodeType.PATIENT, CdrFollowupStatusEnum::getText, null, null),

    REFERRAL("referral", "转诊", CdrNodeType.PATIENT, CdrReferralStatusEnum::getText, null, null),

    PUBLIC_HEALTH("publicHealthReport", "公卫上报", CdrNodeType.PATIENT,
            CdrPublicHealthReportStatusEnum::getText, CdrPublicHealthReportTypeEnum::getText, "上报类型");

    private final String code;
    private final String text;
    private final CdrNodeType nodeType;
    private final Function<Integer, String> statusFn;
    private final Function<Integer, String> secondaryFn;
    /** 副码的中文标签（如"文书类型"）；为空表示这类事件没有副码 */
    private final String secondaryLabel;

    CdrEventType(String code, String text, CdrNodeType nodeType,
                 Function<Integer, String> statusFn, Function<Integer, String> secondaryFn,
                 String secondaryLabel) {
        this.code = code;
        this.text = text;
        this.nodeType = nodeType;
        this.statusFn = statusFn;
        this.secondaryFn = secondaryFn;
        this.secondaryLabel = secondaryLabel;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public CdrNodeType getNodeType() {
        return nodeType;
    }

    public String getSecondaryLabel() {
        return secondaryLabel;
    }

    /** 状态码 → 文案；这类事件没有状态码时返回 null */
    public String statusText(Integer status) {
        return statusFn == null ? null : statusFn.apply(status);
    }

    /** 副码 → 文案；这类事件没有副码时返回 null */
    public String secondaryText(Integer code) {
        return secondaryFn == null ? null : secondaryFn.apply(code);
    }

    /**
     * 未知事件类型不静默丢弃：返回 null，由调用方把原始码值原样展示成"未知事件(xxx)"。
     * 静默丢弃会让"SQL 加了一类事件但枚举忘了同步"这种问题永远发现不了。
     */
    public static CdrEventType parse(String code) {
        for (CdrEventType t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        return null;
    }
}
