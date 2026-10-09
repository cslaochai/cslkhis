package com.his.medicaltech.entity;

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
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 透析单：排班 + 治疗记录一体，一次治疗单元一单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_dialysis_session")
public class BizDialysisSession extends BaseEntity implements Serializable {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 透析单号（HD + yyyyMMdd + 4 位）
     */
    private String sessionNo;

    /**
     * 透析日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dialysisDate;

    /**
     * 时段（1-上午 2-下午 3-夜间）
     */
    private Integer timeSlot;

    /**
     * 机位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long machineId;

    /**
     * 机位号
     */
    private String machineNo;

    /**
     * 透析档案ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long archiveId;

    /**
     * 患者ID
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
     * 使用的透析处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 干体重 kg
     */
    private BigDecimal dryWeight;

    /**
     * 处方透析时长分钟
     */
    private Integer durationMin;

    /**
     * 处方血流量
     */
    private Integer bloodFlow;

    /**
     * 透析器
     */
    private Integer dialyzer;

    /**
     * 抗凝方式
     */
    private Integer anticoagulant;

    /**
     * 状态（1-已排班 2-透析中 3-已完成 4-已取消）
     */
    private Integer status;

    /**
     * 透前体重 kg（上机必填）
     */
    private BigDecimal beforeWeight;

    /**
     * 通路评估（上机必填）
     */
    private String accessCheck;

    /**
     * 上机时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime onTime;

    /**
     * 上机人
     */
    private String onBy;

    /**
     * 透后体重 kg
     */
    private BigDecimal afterWeight;

    /**
     * 实际透析时长分钟
     */
    private Integer actualDurationMin;

    /**
     * 实际超滤量 ml =（透前-透后）×1000，服务端回算
     */
    private BigDecimal ultraMl;

    /**
     * 下机时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime offTime;

    /**
     * 下机人
     */
    private String offBy;

    /**
     * 不良反应类型（，空=无）
     */
    private Integer adverseType;

    /**
     * 不良反应处置描述
     */
    private String adverseDesc;

    /**
     * 取消原因
     */
    private String cancelReason;
}
