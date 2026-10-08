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
 * 住院摆药明细（G13）。
 *
 * <p>状态机：1-待配药 →（药房 FEFO 配药+计费）→ 2-已配药 →（病区核对）→ 3-已核对；
 * 2/3 可退药 → 4-已退药（回库 + 负冲账），**不可逆**。
 *
 * <p>{@code (order_id, dispense_date)} 唯一索引：同一医嘱同日只允许一条**有效**摆药明细，
 * 已退药（4）后允许重新生成（生成排除条件只看未退状态）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ward_dispense_item")
public class BizWardDispenseItem extends BaseEntity implements Serializable {

    /** 明细状态：1-待配药 */
    public static final int STATUS_PENDING = 1;
    /** 明细状态：2-已配药 */
    public static final int STATUS_DISPENSED = 2;
    /** 明细状态：3-已核对 */
    public static final int STATUS_CHECKED = 3;
    /** 明细状态：4-已退药（终态） */
    public static final int STATUS_RETURNED = 4;

    /**
     * 摆药单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispenseId;

    /**
     * 摆药单号（冗余）
     */
    private String dispenseNo;

    /**
     * 摆药日期（同医嘱同日唯一判据）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispenseDate;

    /**
     * 重摆序号（同医嘱同日第几次生成；退药重摆 +1，退药明细留痕不覆盖）
     */
    private Integer dispenseSeq;

    /**
     * 住院医嘱ID（四核对锚点，进收费明细 source_id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /**
     * 医嘱号
     */
    private String orderNo;

    /**
     * 入院ID（冗余）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID（冗余）
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
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 药品ID（药品字典主键，生成时按医嘱项目编码与药品编码匹配；匹配不上不会进摆药单）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 医嘱项目编码
     */
    private String itemCode;

    /**
     * 医嘱项目名称
     */
    private String itemName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 单位
     */
    private String unit;

    /**
     * 摆药数量（长期医嘱为每日量）
     */
    private BigDecimal quantity;

    /**
     * 单价（医嘱开立快照）
     */
    private BigDecimal price;

    /**
     * 金额 = quantity × price
     */
    private BigDecimal amount;

    /**
     * 明细状态：1-待配药 2-已配药 3-已核对 4-已退药
     */
    private Integer status;

    /**
     * 配药前库存（全部批次合计）
     */
    private BigDecimal stockBefore;

    /**
     * 配药后库存
     */
    private BigDecimal stockAfter;

    /**
     * 记账行ID（费用记账流水主键，未记账为 NULL）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;

    /**
     * 记账单号（费用记账流水的单号）
     */
    private String feeNo;

    /**
     * 配药人ID（员工ID，服务端取当前登录人）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispenserId;

    /**
     * 配药人姓名
     */
    private String dispenserName;

    /**
     * 配药时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dispenseTime;

    /**
     * 核对人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long checkerId;

    /**
     * 核对人姓名
     */
    private String checkerName;

    /**
     * 核对时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;

    /**
     * 退药操作人
     */
    private String returnBy;

    /**
     * 退药时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    /**
     * 退药原因（必填）
     */
    private String returnReason;
}
