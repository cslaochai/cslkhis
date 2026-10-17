package com.his.common.enums;

import java.util.function.Function;

/**
 * CDR 时间轴上的**事件类型**表。
 */
public enum CdrEventTypeEnum {

    // 门诊域
    REGIST("regist", "挂号", CdrNodeTypeEnum.OUTPATIENT, AppointStatusEnum::getLabel, null, null),

    OUTPATIENT_RECORD("outpatientRecord", "门诊病历", CdrNodeTypeEnum.OUTPATIENT,
            RecordStatusEnum::getLabel, null, null),

    PRESCRIPTION("prescription", "处方", CdrNodeTypeEnum.OUTPATIENT, CdrPrescriptionStatusEnum::getLabel,
            PrescriptionTypeEnum::getLabel, "处方类型"),

    LAB_APPLY("laboratoryApply", "检验申请", CdrNodeTypeEnum.OUTPATIENT,
            CdrLabApplyStatusEnum::getLabel, null, null),

    LAB_REPORT("laboratoryReport", "检验报告", CdrNodeTypeEnum.OUTPATIENT,
            LabRecordStatusEnum::getLabel, null, null),

    INSP_APPLY("inspectionApply", "检查申请", CdrNodeTypeEnum.OUTPATIENT,
            CdrInspApplyStatusEnum::getLabel, null, null),

    INSP_REPORT("inspectionReport", "检查报告", CdrNodeTypeEnum.OUTPATIENT,
            InsRecordStatusEnum::getLabel, null, null),

    TREATMENT("treatmentApply", "治疗单", CdrNodeTypeEnum.OUTPATIENT,
            TreatmentExecStatusEnum::getLabel, null, null),

    CHARGE("charge", "收费", CdrNodeTypeEnum.OUTPATIENT, CdrChargeStatusEnum::getLabel,
            CdrChargeTypeEnum::getLabel, "收费类型"),

    INSURANCE_SETTLE("insuranceSettlement", "医保结算", CdrNodeTypeEnum.OUTPATIENT,
            CdrInsuranceSettleStatusEnum::getLabel, null, null),

    QUEUE("queue", "候诊叫号", CdrNodeTypeEnum.OUTPATIENT, CdrQueueStatusEnum::getLabel, null, null),

    RECORD_ARCHIVE("recordArchive", "病案归档", CdrNodeTypeEnum.OUTPATIENT,
            ArchiveStatusEnum::getLabel, null, null),

    REPORT_DOC("report", "报告单", CdrNodeTypeEnum.OUTPATIENT, null, null, null),

    // 住院域
    ADMISSION("admission", "入院登记", CdrNodeTypeEnum.INPATIENT, AdmitStatusEnum::getLabel,
            AdmitWayEnum::getLabel, "入院途径"),

    INPATIENT_RECORD("inpatientRecord", "住院文书", CdrNodeTypeEnum.INPATIENT,
            InpatientRecordStatusEnum::getLabel, InpatientRecordTypeEnum::getLabel, "文书类型"),

    INPATIENT_ORDER("inpatientOrder", "住院医嘱", CdrNodeTypeEnum.INPATIENT,
            InpatientOrderStatusEnum::getLabel, OrderTypeEnum::getLabel, "医嘱类型"),

    INPATIENT_DIAGNOSIS("inpatientDiagnosis", "住院诊断", CdrNodeTypeEnum.INPATIENT,
            null, CdrDiagTypeEnum::getLabel, "诊断类型"),

    INPATIENT_SUMMARY("inpatientSummary", "病案首页", CdrNodeTypeEnum.INPATIENT,
            SummaryStatusEnum::getLabel, null, null),

    OPERATION_APPLY("operationApply", "手术申请", CdrNodeTypeEnum.INPATIENT,
            OperationApplyStatusEnum::getLabel, null, null),

    INPATIENT_OPERATION("inpatientOperation", "手术记录", CdrNodeTypeEnum.INPATIENT,
            null, null, null),

    CONSULTATION("consultation", "会诊", CdrNodeTypeEnum.INPATIENT,
            CdrConsultStatusEnum::getLabel, ConsultScopeEnum::getLabel, "会诊范围"),

    TRANSFER("transfer", "转科", CdrNodeTypeEnum.INPATIENT, TransferStatusEnum::getLabel, null, null),

    TRANSFUSION("transfusion", "输血", CdrNodeTypeEnum.INPATIENT,
            TransfusionStatusEnum::getLabel, null, null),

    NURSING_RECORD("nursingRecord", "护理记录", CdrNodeTypeEnum.INPATIENT,
            InpatientRecordStatusEnum::getLabel, NursingDocTypeEnum::getLabel, "文书类型"),

    PREPAY("prepay", "预交金", CdrNodeTypeEnum.INPATIENT, null, PaymentMethodEnum::getLabel, "支付方式"),

    DISCHARGE("discharge", "出院", CdrNodeTypeEnum.INPATIENT, CdrDischargeStatusEnum::getLabel, null, null),

    INPATIENT_SETTLE("inpatientSettlement", "住院结算", CdrNodeTypeEnum.INPATIENT,
            BillStatusEnum::getLabel, null, null),

    // 患者级（跨就诊）
    EMERGENCY("emergency", "急诊", CdrNodeTypeEnum.EMERGENCY, EmergencyStatusEnum::getLabel, null, null),

    CRITICAL_VALUE("criticalValue", "危急值", CdrNodeTypeEnum.PATIENT, CriticalValueStatusEnum::getLabel,
            CriticalTypeEnum::getLabel, "偏离方向"),

    QC("qualityControl", "病案质控", CdrNodeTypeEnum.PATIENT, RuleCheckStatusEnum::getLabel, null, null),

    FOLLOWUP("followupTask", "随访", CdrNodeTypeEnum.PATIENT, FollowupTaskStatusEnum::getLabel, null, null),

    REFERRAL("referral", "转诊", CdrNodeTypeEnum.PATIENT, ReferralStatusEnum::getLabel, null, null),

    PUBLIC_HEALTH("publicHealthReport", "公卫上报", CdrNodeTypeEnum.PATIENT,
            CdrPublicHealthReportStatusEnum::getLabel, CdrPublicHealthReportTypeEnum::getLabel, "上报类型");

    private final String code;
    private final String label;
    private final CdrNodeTypeEnum nodeType;
    private final Function<Integer, String> statusFn;
    private final Function<Integer, String> secondaryFn;
    /**
     * 副码的中文标签（如"文书类型"）；为空表示这类事件没有副码
     */
    private final String secondaryLabel;

    CdrEventTypeEnum(String code, String label, CdrNodeTypeEnum nodeType,
                     Function<Integer, String> statusFn, Function<Integer, String> secondaryFn,
                     String secondaryLabel) {
        this.code = code;
        this.label = label;
        this.nodeType = nodeType;
        this.statusFn = statusFn;
        this.secondaryFn = secondaryFn;
        this.secondaryLabel = secondaryLabel;
    }

    /**
     * 未知事件类型不静默丢弃：返回 null，由调用方把原始码值原样展示成"未知事件(xxx)"。
     * 静默丢弃会让"SQL 加了一类事件但枚举忘了同步"这种问题永远发现不了。
     */
    public static CdrEventTypeEnum fromCode(String code) {
        for (CdrEventTypeEnum t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public CdrNodeTypeEnum getNodeType() {
        return nodeType;
    }

    public String getSecondaryLabel() {
        return secondaryLabel;
    }

    /**
     * 状态码 → 文案；这类事件没有状态码时返回 null
     */
    public String statusText(Integer status) {
        return statusFn == null ? null : statusFn.apply(status);
    }

    /**
     * 副码 → 文案；这类事件没有副码时返回 null
     */
    public String secondaryText(Integer code) {
        return secondaryFn == null ? null : secondaryFn.apply(code);
    }
}