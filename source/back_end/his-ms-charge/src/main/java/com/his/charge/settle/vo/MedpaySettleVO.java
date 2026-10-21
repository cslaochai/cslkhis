package com.his.charge.settle.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 按病种付费结算层 VO（P4）。
 */
public class MedpaySettleVO implements Serializable {

    /**
     * 结算账单 + 病案首页组合行（settleByBill 的入参事实源）
     */
    @Data
    public static class BillRow implements Serializable {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long settleBillId;
        private String billNo;
        private Integer encounterType;
        private Integer billType;
        private String patientName;
        private BigDecimal totalAmount;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long summaryId;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;
        private Integer gender;
        private Integer age;
        private Integer ageUnit;
        private Integer birthWeight;
        private String mainDiagnosisCode;
        private String mainDiagnosisName;
        private Integer isSurgery;
    }

    /**
     * 结算结果行（biz_medpay_settle）
     */
    @Data
    public static class SettleRow implements Serializable {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long settleBillId;
        private String billNo;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long summaryId;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;
        private String patientName;
        private String payMode;
        private String version;
        private String groupCode;
        private String groupName;
        private Integer groupType;
        private BigDecimal weight;
        private BigDecimal rate;
        private BigDecimal baseScore;
        private BigDecimal coeff;
        private BigDecimal score;
        private BigDecimal pointValue;
        private BigDecimal insuranceAmount;
        private BigDecimal actualAmount;
        private BigDecimal profitAmount;
        private Integer calcStatus;
        private String ruleNote;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime settleTime;
    }

    /**
     * 特例单议行（biz_case_appeal）
     */
    @Data
    public static class AppealRow implements Serializable {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String appealNo;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long settleId;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long settleBillId;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long summaryId;
        private String patientName;
        private String payMode;
        private String groupCode;
        private String groupName;
        private Integer reasonType;
        private String reasonDesc;
        private BigDecimal profitAmount;
        private Integer status;
        private String applyBy;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime applyTime;
        private String reviewBy;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reviewTime;
        private String reviewRemark;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    /**
     * 当前医保付费参数（页面头部展示）
     */
    @Data
    public static class ModeVO implements Serializable {
        private String mode;
        private String drgVersion;
        private String dipVersion;
        private String drgRate;
        private String pointValue;
    }

    /**
     * 批量结算汇总
     */
    @Data
    public static class BatchVO implements Serializable {
        private int total;
        private int grouped;
        private int skipped;
        private String skippedNote;
    }
}
