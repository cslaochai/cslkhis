package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检查申请模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inspection_template")
public class BizInspectionTemplate extends BaseEntity {

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 检查项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionItemId;

    /**
     * 检查项目编码
     */
    private String inspectionItemCode;

    /**
     * 检查项目名称
     */
    private String inspectionItemName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 检查目的
     */
    private String inspectionPurpose;

    /**
     * 是否急诊（0-否 1-是）
     */
    private Integer isEmergency;

    /**
     * 排序
     */
    private Integer sortOrder;
}
