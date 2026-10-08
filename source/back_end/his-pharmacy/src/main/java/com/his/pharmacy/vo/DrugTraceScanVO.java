package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 药品追溯码扫码解析结果VO（解析段 + 字典命中 + 批次候选 + 场景校验结论）
 */
@Data
public class DrugTraceScanVO {

    // 码解析
    private String traceCode;
    private Integer codeType;
    private String drugDi;
    private String serialNo;
    private String batchNo;
    /** 有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    /** 是否解析出产品标识（false → 必须人工指定药品） */
    private boolean parsed;

    // 药品字典命中
    private boolean matched;
    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;
    private String drugCode;
    /** 药品名称 */
    private String drugName;
    private String genericName;
    private String specification;
    private String dosageForm;
    /** 单位 */
    private String unit;
    private String manufacturer;
    private String approvalNumber;
    /** 是否要求扫码采集（麻精/集采/医保谈判） */
    private Integer isTraceRequired;
    /** 药品启用状态（0-停用 1-启用） */
    private Integer drugStatus;

    // 该码在台账的现状
    private boolean exists;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long existTraceId;
    private String existTraceNo;
    private Integer existStatus;

    // 批次候选（采集时挂靠用，FEFO 序）
    private List<BatchOption> batches;

    // 发药核销场景：发药单快照
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispensingId;
    private String dispensingNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispensingDrugId;
    private String dispensingDrugName;
    private BigDecimal dispensingQuantity;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispensingPatientId;
    private String dispensingPatientName;

    // 闸门结论
    private boolean canCollect;
    private boolean canDispense;
    private String tip;

    /**
     * 可挂靠批次
     */
    @Data
    public static class BatchOption {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long stockId;
        private String batchNo;
        /** 有效期 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expiryDate;
        private BigDecimal quantity;
        private BigDecimal availableQuantity;
        private Integer stockRoom;
        private String location;
        private String supplier;
    }
}
