package com.his.clinical.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 病历草稿移交代补记录。
 *
 * <p>医生离职/长期休假/调离临床岗位后，名下未提交草稿由科室上级或质控岗承接代补；
 * 病历的经治医生归属不变（快照在病历表），本表只记录「谁把哪份草稿交给谁代补」的留痕与承接权限。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_emr_draft_handover")
public class BizEmrDraftHandover extends BaseEntity {

    /**
     * 逻辑删除标志（0-正常 1-删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 单号 HD+yyyyMMdd+4位
     */
    private String handoverNo;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 病历号（快照）
     */
    private String recordNo;

    /**
     * 患者ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 病历所属科室ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 病历所属科室名称（快照）
     */
    private String deptName;

    /**
     * 原经治医生ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long originalDoctorId;

    /**
     * 原经治医生姓名（快照）
     */
    private String originalDoctorName;

    /**
     * 承接人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiverId;

    /**
     * 承接人姓名（快照）
     */
    private String receiverName;

    /**
     * 移交原因（1-离职 2-长期休假 3-调离临床岗位 4-其他）
     */
    private Integer reason;

    /**
     * 状态（1-待代补 2-已完成 3-已撤销）
     */
    private Integer status;

    /**
     * 移交发起人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 移交发起人姓名（快照）
     */
    private String operatorName;

    /**
     * 代补完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 代补完成人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long completeById;

    /**
     * 代补完成人姓名（快照）
     */
    private String completeByName;

    /**
     * 备注/移交说明
     */
    private String remark;

    /**
     * 非数据库字段：原因文案（出参翻译）
     */
    @TableField(exist = false)
    private String reasonText;

    /**
     * 非数据库字段：状态文案（出参翻译）
     */
    @TableField(exist = false)
    private String statusText;
}
