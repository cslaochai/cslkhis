package com.his.medicaltech.enums;
import com.his.appoint.enums.AppointStatusEnum;
import com.his.common.enums.BillStatusEnum;
import com.his.common.enums.PrescriptionTypeEnum;
import com.his.emr.enums.FollowupTaskStatusEnum;
import com.his.medicaltech.enums.CriticalValueStatusEnum;
import com.his.medicaltech.enums.InsRecordStatusEnum;
import com.his.operation.enums.OperationApplyStatusEnum;
import com.his.patient.enums.ConsultScopeEnum;
import com.his.patient.enums.InpatientOrderStatusEnum;
import com.his.patient.enums.InpatientRecordTypeEnum;
import com.his.patient.enums.NursingDocTypeEnum;
import com.his.patient.enums.OrderTypeEnum;
import com.his.patient.enums.TransferStatusEnum;

import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.PaymentMethodEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.emr.enums.ArchiveStatusEnum;
import com.his.emr.enums.TreatmentExecStatusEnum;
import com.his.patient.enums.AdmitWayEnum;
import com.his.patient.enums.InpatientRecordStatusEnum;
import com.his.patient.enums.ReferralStatusEnum;
import com.his.patient.enums.SummaryStatusEnum;

import java.util.function.Function;
import com.his.medicaltech.enums.CdrChargeStatusEnum;
import com.his.medicaltech.enums.CdrChargeTypeEnum;
import com.his.medicaltech.enums.CdrConsultStatusEnum;
import com.his.medicaltech.enums.CdrDiagTypeEnum;
import com.his.medicaltech.enums.CdrDischargeStatusEnum;
import com.his.common.enums.EmergencyStatusEnum;
import com.his.medicaltech.enums.CdrEmergencyTriageEnum;
import com.his.medicaltech.enums.CdrEventTypeEnum;
import com.his.medicaltech.enums.CdrInspApplyStatusEnum;
import com.his.medicaltech.enums.CdrInsuranceSettleStatusEnum;
import com.his.medicaltech.enums.CdrLabApplyStatusEnum;
import com.his.medicaltech.enums.CdrPrescriptionStatusEnum;
import com.his.medicaltech.enums.CdrPublicHealthReportStatusEnum;
import com.his.medicaltech.enums.CdrPublicHealthReportTypeEnum;
import com.his.medicaltech.enums.CdrQueueStatusEnum;
import com.his.emr.enums.QcStatusEnum;

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
public enum CdrEventTypeEnum {

    // 门诊域
    REGIST("regist", "挂号", CdrNodeTypeEnum.OUTPATIENT, AppointStatusEnum::getText, null, null),

    OUTPATIENT_RECORD("outpatientRecord", "门诊病历", CdrNodeTypeEnum.OUTPATIENT,
            RecordStatusEnum::getText, null, null),

    PRESCRIPTION("prescription", "处方", CdrNodeTypeEnum.OUTPATIENT, CdrPrescriptionStatusEnum::getText,
            PrescriptionTypeEnum::getText, "处方类型"),

    LAB_APPLY("laboratoryApply", "检验申请", CdrNodeTypeEnum.OUTPATIENT,
            CdrLabApplyStatusEnum::getText, null, null),

    LAB_REPORT("laboratoryReport", "检验报告", CdrNodeTypeEnum.OUTPATIENT,
            LabRecordStatusEnum::getText, null, null),

    INSP_APPLY("inspectionApply", "检查申请", CdrNodeTypeEnum.OUTPATIENT,
            CdrInspApplyStatusEnum::getText, null, null),

    INSP_REPORT("inspectionReport", "检查报告", CdrNodeTypeEnum.OUTPATIENT,
            InsRecordStatusEnum::getText, null, null),

    TREATMENT("treatmentApply", "治疗单", CdrNodeTypeEnum.OUTPATIENT,
            TreatmentExecStatusEnum::getText, null, null),

    CHARGE("charge", "收费", CdrNodeTypeEnum.OUTPATIENT, CdrChargeStatusEnum::getText,
            CdrChargeTypeEnum::getText, "收费类型"),

    INSURANCE_SETTLE("insuranceSettlement", "医保结算", CdrNodeTypeEnum.OUTPATIENT,
            CdrInsuranceSettleStatusEnum::getText, null, null),

    QUEUE("queue", "候诊叫号", CdrNodeTypeEnum.OUTPATIENT, CdrQueueStatusEnum::getText, null, null),

    RECORD_ARCHIVE("recordArchive", "病案归档", CdrNodeTypeEnum.OUTPATIENT,
            ArchiveStatusEnum::getText, null, null),

    REPORT_DOC("report", "报告单", CdrNodeTypeEnum.OUTPATIENT, null, null, null),

    // 住院域
    ADMISSION("admission", "入院登记", CdrNodeTypeEnum.INPATIENT, AdmitStatusEnum::getText,
            AdmitWayEnum::getText, "入院途径"),

    INPATIENT_RECORD("inpatientRecord", "住院文书", CdrNodeTypeEnum.INPATIENT,
            InpatientRecordStatusEnum::getText, InpatientRecordTypeEnum::getText, "文书类型"),

    INPATIENT_ORDER("inpatientOrder", "住院医嘱", CdrNodeTypeEnum.INPATIENT,
            InpatientOrderStatusEnum::getText, OrderTypeEnum::getText, "医嘱类型"),

    INPATIENT_DIAGNOSIS("inpatientDiagnosis", "住院诊断", CdrNodeTypeEnum.INPATIENT,
            null, CdrDiagTypeEnum::getText, "诊断类型"),

    INPATIENT_SUMMARY("inpatientSummary", "病案首页", CdrNodeTypeEnum.INPATIENT,
            SummaryStatusEnum::getText, null, null),

    OPERATION_APPLY("operationApply", "手术申请", CdrNodeTypeEnum.INPATIENT,
            OperationApplyStatusEnum::getText, null, null),

    INPATIENT_OPERATION("inpatientOperation", "手术记录", CdrNodeTypeEnum.INPATIENT,
            null, null, null),

    CONSULTATION("consultation", "会诊", CdrNodeTypeEnum.INPATIENT,
            CdrConsultStatusEnum::getText, ConsultScopeEnum::getText, "会诊范围"),

    TRANSFER("transfer", "转科", CdrNodeTypeEnum.INPATIENT, TransferStatusEnum::getText, null, null),

    TRANSFUSION("transfusion", "输血", CdrNodeTypeEnum.INPATIENT,
            TransfusionStatusEnum::getText, null, null),

    NURSING_RECORD("nursingRecord", "护理记录", CdrNodeTypeEnum.INPATIENT,
            InpatientRecordStatusEnum::getText, NursingDocTypeEnum::getText, "文书类型"),

    PREPAY("prepay", "预交金", CdrNodeTypeEnum.INPATIENT, null, PaymentMethodEnum::getText, "支付方式"),

    DISCHARGE("discharge", "出院", CdrNodeTypeEnum.INPATIENT, CdrDischargeStatusEnum::getText, null, null),

    INPATIENT_SETTLE("inpatientSettlement", "住院结算", CdrNodeTypeEnum.INPATIENT,
            BillStatusEnum::getText, null, null),

    // 患者级（跨就诊）
    EMERGENCY("emergency", "急诊", CdrNodeTypeEnum.EMERGENCY, EmergencyStatusEnum::getText, null, null),

    CRITICAL_VALUE("criticalValue", "危急值", CdrNodeTypeEnum.PATIENT, CriticalValueStatusEnum::getText,
            CriticalTypeEnum::getText, "偏离方向"),

    QC("qualityControl", "病案质控", CdrNodeTypeEnum.PATIENT, QcStatusEnum::getText, null, null),

    FOLLOWUP("followupTask", "随访", CdrNodeTypeEnum.PATIENT, FollowupTaskStatusEnum::getText, null, null),

    REFERRAL("referral", "转诊", CdrNodeTypeEnum.PATIENT, ReferralStatusEnum::getText, null, null),

    PUBLIC_HEALTH("publicHealthReport", "公卫上报", CdrNodeTypeEnum.PATIENT,
            CdrPublicHealthReportStatusEnum::getText, CdrPublicHealthReportTypeEnum::getText, "上报类型");

    private final String code;
    private final String text;
    private final CdrNodeTypeEnum nodeType;
    private final Function<Integer, String> statusFn;
    private final Function<Integer, String> secondaryFn;
    /**
     * 副码的中文标签（如"文书类型"）；为空表示这类事件没有副码
     */
    private final String secondaryLabel;

    CdrEventTypeEnum(String code, String text, CdrNodeTypeEnum nodeType,
                     Function<Integer, String> statusFn, Function<Integer, String> secondaryFn,
                     String secondaryLabel) {
        this.code = code;
        this.text = text;
        this.nodeType = nodeType;
        this.statusFn = statusFn;
        this.secondaryFn = secondaryFn;
        this.secondaryLabel = secondaryLabel;
    }

    /**
     * 未知事件类型不静默丢弃：返回 null，由调用方把原始码值原样展示成"未知事件(xxx)"。
     * 静默丢弃会让"SQL 加了一类事件但枚举忘了同步"这种问题永远发现不了。
     */
    public static CdrEventTypeEnum parse(String code) {
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

    public String getText() {
        return text;
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