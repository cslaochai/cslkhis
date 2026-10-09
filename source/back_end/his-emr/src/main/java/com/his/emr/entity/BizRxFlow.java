package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 处方流转单实体（M2，院外取药口子）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_rx_flow")
public class BizRxFlow extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 流转单号
     */
    private String flowNo;

    /**
     * 处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

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
     * 流向机构名称
     */
    private String orgName;

    /**
     * 机构类型（1-院外药店 2-基层医疗机构 3-线上药房）
     */
    private Integer orgType;

    /**
     * 流转状态（1-已流转 2-已取药 3-已取消）
     */
    private Integer flowStatus;

    /**
     * 流转时间
     */
    private LocalDateTime flowTime;

    /**
     * 完成/取消时间
     */
    private LocalDateTime finishTime;

    /**
     * 处方总金额
     */
    private BigDecimal totalAmount;
}
