package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 可静配医嘱候选（生成静配单前的预览）。
 */
@Data
public class PivasCandidateVO implements Serializable {

    /** 住院医嘱ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /** 医嘱号（快照） */
    private String orderNo;

    /** 入院ID（冗余） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID（冗余） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 病区ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 医嘱类型：1-长期 2-临时
     */
    private Integer orderType;

    /** 医嘱项目编码（快照） */
    private String itemCode;

    /** 医嘱项目名称（快照） */
    private String itemName;

    /** 规格（快照） */
    private String spec;

    /** 单位 */
    private String unit;

    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal price;

    private BigDecimal amount;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 药品名称（快照） */
    private String drugName;

    /**
     * 给药途径（中文原文：静滴/静推/泵入…）
     */
    private String route;

    /**
     * 频次（qd/bid/tid…）
     */
    private String frequency;
}
