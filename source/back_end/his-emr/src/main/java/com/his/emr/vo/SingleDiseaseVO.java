package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 单病种质控出参集合（M4）。
 */
public class SingleDiseaseVO {

    /**
     * 病种目录
     */
    @Data
    public static class Disease {
        /**
         * 主键
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /**
         * 病种编码
         */
        private String diseaseCode;
        /**
         * 病种名称
         */
        private String diseaseName;
        /**
         * 纳入 ICD-10 前缀
         */
        private String icd10Prefix;
        /**
         * 已纳入病例数
         */
        private Long caseCount;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 病例
     */
    @Data
    public static class Case {
        /**
         * 主键
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String caseNo;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long diseaseId;
        /**
         * 病种名称
         */
        private String diseaseName;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;
        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /**
         * 患者姓名
         */
        private String patientName;
        private String mainDiagnosisCode;
        private String mainDiagnosisName;
        private Integer inpatientDays;
        /**
         * 合计金额
         */
        private BigDecimal totalAmount;
        private Integer isSurgery;
        private Integer deathFlag;
        private Integer curativeEffect;
        private Integer enrollWay;
        private Integer qcStatus;
        private String qcIssues;
        private Integer reportStatus;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime reportTime;
        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 病种质控指标（服务端复算口径）
     */
    @Data
    public static class Metric {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long diseaseId;
        /**
         * 病种编码
         */
        private String diseaseCode;
        /**
         * 病种名称
         */
        private String diseaseName;
        /**
         * 纳入例数
         */
        private Long caseCount;
        /**
         * 治愈例数（疗效=1）
         */
        private Long curedCount;
        /**
         * 治愈率（治愈/纳入，保留 4 位小数）
         */
        private BigDecimal cureRate;
        /**
         * 死亡例数
         */
        private Long deathCount;
        /**
         * 死亡率
         */
        private BigDecimal deathRate;
        /**
         * 平均住院日（天，2 位）
         */
        private BigDecimal avgInpatientDays;
        /**
         * 平均住院费用（元，2 位）
         */
        private BigDecimal avgTotalAmount;
        /**
         * 已质控例数
         */
        private Long qcPassedCount;
    }

}
