package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院详情 VO
 */
@Data
public class InpatientDetailVO {

    /**
     * 入院信息
     */
    private AdmissionInfo admission;

    /**
     * 病案首页（尚未生成时为空）
     */
    private SummaryInfo summary;

    /**
     * 病案首页状态文案（草稿/已提交/已归档），由后端统一给，前端不再自己拼
     */
    private String summaryStatusText;

    /**
     * 诊断明细（已按主要诊断 → 其他诊断排序）
     */
    private List<DiagnosisInfo> diagnoses;

    /**
     * 手术操作明细
     */
    private List<OperationInfo> operations;

    /**
     * 入院信息
     */
    @Data
    public static class AdmissionInfo {

        /**
         * 入院ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        private String admissionNo;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 患者编号
         */
        private String patientNo;
        /**
         * 患者姓名
         */
        private String patientName;
        /**
         * 性别（1-男 2-女 9-未知）
         */
        private Integer gender;
        /**
         * 年龄
         */
        private Integer age;
        private String idCard;
        /**
         * 医保卡号
         */
        private String medicalInsuranceNo;
        private String insuranceType;

        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;

        /**
         * 科室名称
         */
        private String deptName;

        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;

        /**
         * 病区名称
         */
        private String wardName;

        /**
         * 床位ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long bedId;

        /**
         * 床位号
         */
        private String bedNo;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long admitDoctorId;

        /**
         * 医生姓名
         */
        private String doctorName;

        /**
         * 入院时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime admitTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dischargeTime;

        private Integer admitWay;

        /**
         * 状态：0-已出院 1-在院
         */
        private Integer admitStatus;

        /**
         * 诊断
         */
        private String diagnosis;
        /**
         * 备注
         */
        private String remark;

        /**
         * 住院天数（在院时为已住院天数）
         */
        private Integer inpatientDays;

        // 门诊来源线索（门诊转住院的追溯依据）

        /**
         * 就诊次ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long visitId;

        /**
         * 来源挂号ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long registId;

        /**
         * 来源挂号号
         */
        private String registNo;

        /**
         * 来源住院证ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionOrderId;

        /**
         * 来源住院证号（门诊转住院才有值；直接入院为空）
         */
        private String admissionOrderNo;
    }

    /**
     * 病案首页
     */
    @Data
    public static class SummaryInfo {

        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 入院ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        private Integer ageUnit;
        private Integer admitWay;
        private Integer dischargeWay;
        private Integer deathFlag;
        private Integer readmit31d;
        private Integer isSurgery;
        private Integer isRescue;
        private Integer isCritical;
        private String mainDiagnosisCode;
        private String mainDiagnosisName;
        private Integer inpatientDays;

        /**
         * 合计金额
         */
        private BigDecimal totalAmount;
        private BigDecimal westernDrugAmount;
        private BigDecimal chineseDrugAmount;
        private BigDecimal herbalAmount;
        private BigDecimal examAmount;
        private BigDecimal labAmount;
        private BigDecimal treatmentAmount;
        private BigDecimal operationAmount;
        private BigDecimal materialAmount;
        private BigDecimal bedAmount;
        private BigDecimal nursingAmount;
        private BigDecimal otherAmount;

        private Integer summaryStatus;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 诊断明细项
     */
    @Data
    public static class DiagnosisInfo {

        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private Integer seqNo;

        /**
         * 1-主要诊断 2-其他诊断
         */
        private Integer diagType;

        private String icdCode;
        private String icdName;

        /**
         * 入院病情：1-有 2-临床未确定 3-情况不明 4-无
         */
        private Integer admitCondition;

        /**
         * NONE / CC / MCC
         */
        private String ccLevel;

        private String diagnosisBasis;
    }

    /**
     * 手术操作明细项
     */
    @Data
    public static class OperationInfo {

        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        private Integer seqNo;
        private Integer isMain;
        private String operationCode;
        private String operationName;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime operationDate;

        private Integer operationLevel;
        private Integer incisionLevel;
        private Integer anesthesiaType;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long surgeonId;

        private String surgeonName;
        private String assistantName;
        private String operationBasis;
    }
}
