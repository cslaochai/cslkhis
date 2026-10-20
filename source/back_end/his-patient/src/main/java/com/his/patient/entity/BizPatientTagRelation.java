package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

/**
 * 患者标签关联
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_patient_tag_relation")
public class BizPatientTagRelation extends BaseEntity {

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

}