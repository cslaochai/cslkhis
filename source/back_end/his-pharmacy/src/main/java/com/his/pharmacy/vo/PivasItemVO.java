package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 静配明细行（主单详情内嵌）。
 */
@Data
public class PivasItemVO implements Serializable {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 主单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pivasId;

    /** 静配单号（冗余） */
    private String pivasNo;

    /** 住院医嘱ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /** 医嘱号 */
    private String orderNo;

    /** 入院ID（冗余） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID（冗余） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 药品名称 */
    private String drugName;

    /** 医嘱项目编码 */
    private String itemCode;

    /** 医嘱项目名称 */
    private String itemName;

    /** 规格 */
    private String spec;

    /** 单位 */
    private String unit;

    /** 当日调配数量 */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal price;

    /** 金额 = quantity × price */
    private BigDecimal amount;

    /** 给药途径（快照，中文原文：静滴/静推/泵入…） */
    private String route;

    /** 频次（快照，qd/bid/tid…） */
    private String frequency;

    /**
     * 明细状态：0-已拒配 1-待审方 2-已审方 3-已排队 4-已调配 5-已核对发放
     */
    private Integer status;

    /**
     * 排队号（调配日内全局递增）
     */
    private Integer queueNo;

    /** 审方药师姓名 */
    private String auditorName;

    /** 审方时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 审方退回原因 */
    private String rejectReason;

    /** 调配人姓名 */
    private String compounderName;

    /** 调配时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime compoundTime;

    /** 成品核对人姓名 */
    private String verifierName;

    /** 核对发放时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifyTime;

    /** 备注 */
    private String remark;
}
