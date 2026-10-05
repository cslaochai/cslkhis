package com.his.emr.support;

import com.his.emr.entity.BizMedicalRecord;
import com.his.patient.entity.BizInpatientRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 质控对象快照 —— 把门诊病历与住院文书归一成同一套被检查字段。
 *
 * <p><b>为什么要有这一层</b>：两张表字段名差不多但并不相同
 * （门诊用 {@code diagnosis} 存诊断文本、血压是 varchar；
 * 住院用 {@code diagnosis_name}、血压是 int）。规则如果直接吃两套实体，
 * 每条规则都要写两遍分支，迟早出现"门诊加了新规则、住院那半边忘了改"。
 *
 * <p><b>数值字段一律宽松解析</b>：门诊的 {@code pulse}/{@code systolic_pressure} 是
 * {@code varchar(10)}，里面可能写「72」也可能写「72次/分」。解析不了就视为"未记录"
 * 而不是 0 —— 把无法解析的值当成越界去报缺陷，是典型的假警报。
 */
@Data
public class QcSnapshot {

    private QcRecordSource source;

    private Long recordId;

    private String recordNo;

    private Long patientId;

    private String patientName;

    private Integer gender;

    private Integer age;

    private String deptName;

    private Long doctorId;

    private String doctorName;

    /**
     * 住院文书类型；门诊病历为 null
     */
    private Integer recordType;

    private Integer recordStatus;

    private String chiefComplaint;

    private String presentIllness;

    private String pastHistory;

    private String allergyHistory;

    private String diagnosisName;

    private String diagnosisCode;

    /**
     * 诊断文本（门诊取 diagnosis，住院取 diagnosis_name），用于性别-诊断矛盾检查
     */
    private String diagnosisText;

    private String treatmentPlan;

    /**
     * 文书正文（仅住院）
     */
    private String courseNote;

    private String recordTitle;

    private BigDecimal temperature;

    private Integer pulse;

    private Integer respiration;

    private Integer systolicPressure;

    private Integer diastolicPressure;

    private LocalDateTime recordTime;

    private LocalDateTime submitTime;

    private LocalDateTime archiveTime;

    /**
     * 门诊病历快照
     */
    public static QcSnapshot ofOutpatient(BizMedicalRecord r) {
        QcSnapshot s = new QcSnapshot();
        s.source = QcRecordSource.OUTPATIENT;
        s.recordId = r.getId();
        s.recordNo = r.getRecordNo();
        s.patientId = r.getPatientId();
        s.patientName = r.getPatientName();
        s.gender = r.getGender();
        s.age = r.getAge();
        s.deptName = r.getDeptName();
        s.doctorId = r.getDoctorId();
        s.doctorName = r.getDoctorName();
        s.recordType = null;
        s.recordStatus = r.getRecordStatus();
        s.chiefComplaint = r.getChiefComplaint();
        s.presentIllness = r.getPresentIllness();
        s.pastHistory = r.getPastHistory();
        s.allergyHistory = r.getAllergyHistory();
        s.diagnosisName = r.getDiagnosisName();
        s.diagnosisCode = r.getDiagnosisCode();
        // 门诊的 diagnosis 是诊断文本；缺失时退回 diagnosis_name
        s.diagnosisText = hasText(r.getDiagnosis()) ? r.getDiagnosis() : r.getDiagnosisName();
        s.treatmentPlan = r.getTreatmentPlan();
        s.temperature = parseDecimal(r.getTemperature());
        s.pulse = parseInteger(r.getPulse());
        s.respiration = parseInteger(r.getRespiration());
        s.systolicPressure = parseInteger(r.getSystolicPressure());
        s.diastolicPressure = parseInteger(r.getDiastolicPressure());
        // 门诊没有 record_time，就诊日期即记录时间
        s.recordTime = r.getVisitDate() == null ? null : r.getVisitDate().atStartOfDay();
        s.submitTime = r.getSubmitTime();
        // 门诊没有归档时间列（归档另记病历归档），留空即"未归档"，
        // 归档类规则（L05/L06）本来也只适用于住院文书
        s.archiveTime = null;
        return s;
    }

    /**
     * 住院文书快照
     */
    public static QcSnapshot ofInpatient(BizInpatientRecord r) {
        QcSnapshot s = new QcSnapshot();
        s.source = QcRecordSource.INPATIENT;
        s.recordId = r.getId();
        s.recordNo = r.getRecordNo();
        s.patientId = r.getPatientId();
        s.patientName = r.getPatientName();
        s.gender = r.getGender();
        s.age = r.getAge();
        s.deptName = r.getDeptName();
        s.doctorId = r.getDoctorId();
        s.doctorName = r.getDoctorName();
        s.recordType = r.getRecordType();
        s.recordStatus = r.getRecordStatus();
        s.chiefComplaint = r.getChiefComplaint();
        s.presentIllness = r.getPresentIllness();
        s.pastHistory = r.getPastHistory();
        s.allergyHistory = r.getAllergyHistory();
        s.diagnosisName = r.getDiagnosisName();
        s.diagnosisCode = r.getDiagnosisCode();
        s.diagnosisText = r.getDiagnosisName();
        s.treatmentPlan = r.getTreatmentPlan();
        s.courseNote = r.getCourseNote();
        s.recordTitle = r.getRecordTitle();
        s.temperature = r.getTemperature();
        s.pulse = r.getPulse();
        s.respiration = r.getRespiration();
        s.systolicPressure = r.getSystolicPressure();
        s.diastolicPressure = r.getDiastolicPressure();
        s.recordTime = r.getRecordTime();
        s.submitTime = r.getSubmitTime();
        s.archiveTime = r.getArchiveTime();
        return s;
    }

    private static boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    private static BigDecimal parseDecimal(String text) {
        if (!hasText(text)) {
            return null;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException ex) {
            // 自由文本（如「36.5℃」）解析不了就视为未记录，不要当成越界
            return null;
        }
    }

    private static Integer parseInteger(String text) {
        if (!hasText(text)) {
            return null;
        }
        // 先原样试，再剥掉非数字字符试（「72次/分」）
        String trimmed = text.trim();
        try {
            return Integer.valueOf(trimmed);
        } catch (NumberFormatException ignored) {
            String digits = trimmed.replaceAll("[^0-9-]", "");
            if (digits.isEmpty() || "-".equals(digits)) {
                return null;
            }
            try {
                return Integer.valueOf(digits);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    /**
     * 是否为住院入院记录（record_type = 1）
     */
    public boolean isInpatientEntry() {
        return source == QcRecordSource.INPATIENT && recordType != null && recordType == 1;
    }

    /**
     * 是否为住院记录类文书（非入院记录）
     */
    public boolean isInpatientNote() {
        return source == QcRecordSource.INPATIENT && !isInpatientEntry();
    }

    /**
     * 文书类型中文名，用于问题描述里定位"是哪份文书"
     */
    public String recordTypeText() {
        if (source == QcRecordSource.OUTPATIENT) {
            return "门诊病历";
        }
        return recordType == null ? "住院文书" : QcTexts.recordType(recordType);
    }
}
