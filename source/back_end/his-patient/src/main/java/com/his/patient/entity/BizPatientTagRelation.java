package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 患者标签关联
 */
@Data
@TableName("biz_patient_tag_relation")
public class BizPatientTagRelation {
    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 患者ID (关联患者主表)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 标签ID (关联标签字典表)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tagId;

    /**
     * 标签来源：1-手动打标, 2-系统自动打标
     */
    private Integer sourceType;

    /**
     * 打标时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}