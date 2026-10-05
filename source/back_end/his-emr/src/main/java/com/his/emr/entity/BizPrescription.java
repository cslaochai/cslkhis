package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 处方主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_prescription")
public class BizPrescription extends BaseEntity {
    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 病历号
     */
    private String recordNo;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;

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
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 开方医师签名ID（电子签名证据的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorSignId;

    /**
     * 开方签名时刻
     */
    private LocalDateTime doctorSignedTime;

    /**
     * 处方类型（1-西药处方 2-中成药处方 3-中药饮片处方）
     */
    private Integer prescriptionType;

    /**
     * 处方来源（1-门诊处方 2-急诊处方 3-住院处方）
     */
    private Integer prescriptionSource;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 药品数量
     */
    private Integer drugCount;

    /**
     * 用法说明
     */
    private String usageInstruction;

    /**
     * 中药饮片剂数（sql/139：处方类型=3 必填，1~30；其余类型为 NULL）。
     * <p>剂数只在处方头存一份 —— 一张方是「一剂药 × N 剂」，每味药的克数是<b>每剂</b>的量，
     * 摊到明细上会出现同一方 12 味 12 个剂数、互相不一致。
     */
    private Integer doseCount;

    /**
     * 中药煎服方式（字典 his_tcm_decoct_flag：1-代煎 2-自煎；仅饮片方使用）
     */
    private Integer decoctFlag;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 处方状态（1-草稿 2-已提交 3-已审核 4-已发药 5-已取消 6-已退药）
     */
    private Integer prescriptionStatus;

    /**
     * 缴费状态（0-未缴费 1-已缴费 2-已退费）
     */
    private Integer paymentStatus;

    /**
     * 长处方（M1 慢病长处方）：0-否 1-是。置 1 需患者存在已认定的慢病档案（服务端校验）
     */
    private Integer isLongPrescription;

    /**
     * 长处方用药天数（≤90）
     */
    private Integer longPrescriptionDays;

    /**
     * 缴费时间
     */
    private LocalDateTime payTime;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付方式（1-现金 2-微信 3-支付宝 4-医保卡 5-余额）
     */
    private Integer payMethod;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核结果（1通过/2退回；未审为 NULL）—— L7 审方退回重开闭环
     */
    private Integer auditResult;

    /**
     * 最近一次审方退回原因
     */
    private String returnReason;

    /**
     * 最近一次退回时间
     */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    /**
     * 累计被退回次数
     */
    private Integer returnCount;

    /**
     * 审核人（审方药师姓名）
     */
    private String auditBy;

    /**
     * 审方药师签名ID（电子签名证据的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditSignId;

    /**
     * 审方签名时刻
     */
    private LocalDateTime auditSignedTime;

    /**
     * 发药时间
     */
    private LocalDateTime dispenseTime;

    /**
     * 发药人
     */
    private String dispenseBy;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 退药时间
     */
    private LocalDateTime refundTime;

    /**
     * 退药人
     */
    private String refundBy;

    /**
     * 退药原因
     */
    private String refundReason;

    /**
     * 是否加急（0-否 1-是）
     */
    private Integer isUrgent;

    @TableField(exist = false)
    private List<BizPrescriptionDetail> details;
}
