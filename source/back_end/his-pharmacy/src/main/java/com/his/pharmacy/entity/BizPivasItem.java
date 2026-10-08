package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 静配中心调配明细（PIVAS）：一行 = 医嘱 × 调配日。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pivas_item")
public class BizPivasItem extends BaseEntity implements Serializable {

    /** 明细状态：0-已拒配（审方退回终态，可重排入单） */
    public static final int STATUS_REJECTED = 0;
    /** 明细状态：1-待审方 */
    public static final int STATUS_PENDING_AUDIT = 1;
    /** 明细状态：2-已审方（待打标签排队） */
    public static final int STATUS_AUDITED = 2;
    /** 明细状态：3-已排队（待调配） */
    public static final int STATUS_QUEUED = 3;
    /** 明细状态：4-已调配（成品待核对） */
    public static final int STATUS_COMPOUNDED = 4;
    /** 明细状态：5-已核对发放（终态） */
    public static final int STATUS_VERIFIED = 5;

    /** 主单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pivasId;

    /**
     * 静配单号（冗余）
     */
    private String pivasNo;

    /**
     * 调配日期（同医嘱同日唯一判据之一）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate admixDate;

    /**
     * 重生成序号（同医嘱同日第几次入单；拒配后重排入 +1）
     */
    private Integer pivasSeq;

    /**
     * 住院医嘱ID（住院医嘱主表主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /**
     * 医嘱号
     */
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

    /** 病区ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 药品名称
     */
    private String drugName;

    /** 医嘱项目编码 */
    private String itemCode;

    /** 医嘱项目名称 */
    private String itemName;

    /** 规格 */
    private String spec;

    /** 单位 */
    private String unit;

    /**
     * 当日调配数量（长期医嘱为每日量）
     */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal price;

    /** 金额 = quantity × price */
    private BigDecimal amount;

    /**
     * 给药途径（快照，中文原文：静滴/静推/泵入…）
     */
    private String route;

    /**
     * 频次（快照，qd/bid/tid…）
     */
    private String frequency;

    /** 明细状态（0-已拒配 1-待审方 2-已审方 3-已排队 4-已调配 5-已核对发放） */
    private Integer status;

    /**
     * 排队号（调配日内全局递增，中心叫号口径）
     */
    private Integer queueNo;

    /** 审方药师ID（员工ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditorId;

    /** 审方药师姓名 */
    private String auditorName;

    /** 审方时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 审方退回原因（status=0 必填）
     */
    private String rejectReason;

    /** 调配人ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long compounderId;

    /** 调配人姓名 */
    private String compounderName;

    /** 调配时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime compoundTime;

    /** 成品核对人ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long verifierId;

    /** 成品核对人姓名 */
    private String verifierName;

    /** 核对发放时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifyTime;
}
