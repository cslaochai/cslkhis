package com.mis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 2304 结算信息上传报文（字段与 HIS 报盘口径一致，逐字段接收其 payload）
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadSettlementDTO {

    private String msgType;
    private String tradeNo;
    private String fixMedinsCode;
    private String fixMedinsName;
    private String settlementNo;
    private String sendTime;

    private Patient patient;
    private Visit visit;
    private Fees fees;
    private Fund fund;
    private List<Item> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Patient {
        private String patientNo;
        private String patientName;
        private String gender;
        private String age;
        private String idCard;
        private String insuranceNo;
        private String insuranceType;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Visit {
        private String billNo;
        private String encounterType;
        private String visitType;
        private String deptName;
        private String doctorName;
        private String diagnosisCode;
        private String diagnosisName;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fees {
        private BigDecimal total;
        private BigDecimal drug;
        private BigDecimal inspection;
        private BigDecimal laboratory;
        private BigDecimal treatment;
        private BigDecimal material;
        private BigDecimal other;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fund {
        private BigDecimal coverageRatio;
        private BigDecimal insurancePay;
        private BigDecimal personalPay;
        private BigDecimal selfPay;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Integer itemType;
        private String itemTypeName;
        private String itemCode;
        private String itemName;
        private String specification;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal amount;
        private BigDecimal discount;
        /** 目录类别（甲/乙/自费） */
        private Integer catalogType;
        private BigDecimal pool;
        private BigDecimal self;
    }
}
