package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 检查胶片用量与发放（检查胶片用量，sql/138）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_exam_film")
public class BizExamFilm extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 胶片单号（FM+yyyyMMdd+5位，唯一）
     */
    private String filmNo;

    /**
     * 检查记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 检查记录号
     */
    private String recordNo;

    /**
     * 检查申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 申请单号
     */
    private String applyNo;

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
     * 就诊日期
     */
    private LocalDate visitDate;

    /**
     * 检查项目编码
     */
    private String itemCode;

    /**
     * 检查项目名称
     */
    private String itemName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 影像模态（字典 his_exam_device_type，快照）
     */
    private Integer modality;

    /**
     * 胶片规格ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long specId;

    /**
     * 规格编码
     */
    private String specCode;

    /**
     * 规格名称（快照，列表直接显示，不反查价目表）
     */
    private String specName;

    /**
     * 单价（记账当时的规格价，快照）
     */
    private BigDecimal unitPrice;

    /**
     * 计价单位
     */
    private String unit;

    /**
     * 胶片张数
     */
    private Integer quantity;

    /**
     * 金额 = 单价 × 张数（服务端现算；改错走红冲，不 UPDATE 这一列）
     */
    private BigDecimal amount;

    /**
     * 状态（字典 his_film_status：1-已登记 2-已打印 3-已发放 4-已作废）
     */
    private Integer filmStatus;

    /**
     * 是否已记账（0-未记账 1-已记账）
     */
    private Integer chargeFlag;

    /**
     * 记账流水ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeId;

    /**
     * 记账流水号
     */
    private String feeNo;

    /**
     * 打印人
     */
    private String printBy;

    /**
     * 打印时间
     */
    private LocalDateTime printTime;

    /**
     * 发放人（交给患者/病区的那个人）
     */
    private String deliverBy;

    /**
     * 发放时间
     */
    private LocalDateTime deliverTime;
}
