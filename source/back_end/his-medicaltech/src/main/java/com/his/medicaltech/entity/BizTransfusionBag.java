package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 住院输血血袋明细—— 追溯粒度是「袋」不是「次」。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_transfusion_bag")
public class BizTransfusionBag extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 输血申请单ID（输血申请单的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 入院ID（冗余，便于按住院聚合追溯）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID（冗余）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 血袋号（全局流水号；一袋血只能给一个人）
     */
    private String bagNo;

    /**
     * 献血编号（献血者条码，倒查血液来源用）
     */
    private String donorNo;

    /**
     * 血袋ABO血型：A/B/O/AB
     */
    private String bagAbo;

    /**
     * 血袋Rh血型：阳/阴
     */
    private String bagRh;

    /**
     * 血液品种（应与申请单一致）
     */
    private Integer bloodComponent;

    /**
     * 规格
     */
    private String spec;

    /**
     * 血量
     */
    private BigDecimal amount;

    /**
     * 血量单位：U / ml / 治疗量
     */
    private String amountUnit;

    /**
     * 来源血站
     */
    private String sourceBank;

    /**
     * 采集日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate collectDate;

    /**
     * 有效期至（超期血袋不得输注）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;

    /**
     * 主侧配血结果：阴性/阳性（阴性=相合）
     */
    private String crossmatchMain;

    /**
     * 次侧配血结果：阴性/阳性
     */
    private String crossmatchSide;

    /**
     * 配血结论（1-相合 2-不合）
     */
    private Integer crossmatchResult;

    /**
     * 配血时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime crossmatchTime;

    /**
     * 配血人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long crossmatchDoctorId;

    /**
     * 配血人姓名
     */
    private String crossmatchDoctorName;

    /**
     * 血袋状态（0-待配血 1-已配血 2-已发血 3-已输注）
     */
    private Integer bagStatus;

    /**
     * 发血时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issueTime;
}
