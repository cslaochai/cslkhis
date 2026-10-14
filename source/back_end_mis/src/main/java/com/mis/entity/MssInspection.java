package com.mis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 飞检任务
 */
@Data
@TableName("mss_inspection")
public class MssInspection implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 任务单号（唯一） */
    private String inspectionNo;
    /** 任务名称 */
    private String title;
    /** 检查类型（专项检查/日常巡查/举报核查） */
    private String inspectType;
    /** 被查机构 */
    private String targetOrg;
    /** 计划开始日 */
    private LocalDate planStart;
    /** 计划结束日 */
    private LocalDate planEnd;
    /** 带队人 */
    private String leader;
    /** 检查组成员 */
    private String members;
    /** 检查范围 */
    private String scopeDesc;
    /** 状态（草稿/待开展/检查中/已完成） */
    private String status;
    /** 检查结果小结 */
    private String resultSummary;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
