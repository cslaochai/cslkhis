package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 急诊越权授权事后登记实体：一行 = 一次「没授权但因为急诊/抢救做了」。
 *
 * <p>见 {@code sql/155}。择期手术不豁免（闸门直接拒），只有急诊单放行并强制写这一行，
 * 事后由上级医师确认。本表是评审抽查「越权是否事后追认」的唯一事实来源，
 * 因此没有 del_flag，也不允许改金额之外的任何已发生字段。
 */
@Data
@TableName("biz_tech_auth_override")
public class BizTechAuthOverride {

    /**
     * 主键（雪花ID）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 来源单据类型（字典 his_tech_override_source / TechOverrideSourceEnum：1-手术申请 2-日间手术 3-住院医嘱 4-内镜记录）
     */
    private Integer sourceType;

    /**
     * 来源单据ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;

    /**
     * 来源单据号
     */
    private String sourceNo;

    /**
     * 越权操作者（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 越权操作者姓名（快照）
     */
    private String employeeName;

    /**
     * 涉及授权类别（同 his_tech_auth_category）
     */
    private Integer authCategory;

    /**
     * 该操作要求的级别
     */
    private Integer requiredLevel;

    /**
     * 越权者当时的授权级别上限（NULL=完全没有该类别授权）
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer heldLevel;

    /**
     * 越权原因（必填：写清为什么等不起）
     */
    private String reason;

    /**
     * 越权发生时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime occurTime;

    /**
     * 状态（字典 his_tech_override_status：1-待上级确认 2-已确认）
     */
    private Integer overrideStatus;

    /**
     * 上级确认人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long supervisorId;

    /**
     * 上级确认人姓名
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String supervisorName;

    /**
     * 确认时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime confirmTime;

    /**
     * 确认意见
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String confirmOpinion;

    /**
     * 备注
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String remark;
}
