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
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 透析处方。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_dialysis_prescription")
public class BizDialysisPrescription extends BaseEntity implements Serializable {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 透析档案ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long archiveId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 干体重 kg
     */
    private BigDecimal dryWeight;

    /**
     * 单次透析时长（分钟）
     */
    private Integer durationMin;

    /**
     * 血流量 mL/min
     */
    private Integer bloodFlow;

    /**
     * 透析器（1-低通量纤维素膜 2-低通量合成膜 3-高通量合成膜）
     */
    private Integer dialyzer;

    /**
     * 抗凝方式（1-普通肝素 2-低分子肝素 3-枸橼酸钠 4-无肝素）
     */
    private Integer anticoagulant;

    /**
     * 抗凝剂量描述
     */
    private String anticoagDose;

    /**
     * 目标超滤量 ml
     */
    private BigDecimal targetUltraMl;

    /**
     * 处方生效日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 处方停用日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 状态（1-有效 2-已停用）
     */
    private Integer status;

    /**
     * 开立医生（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 开立医生姓名
     */
    private String doctorName;

    /**
     * 停用原因
     */
    private String stopReason;
}
