package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 检验申请模板 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_laboratory_template")
public class BizLaboratoryTemplate extends BaseEntity {
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
     * 检验项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long laboratoryItemId;

    /**
     * 检验项目编码
     */
    private String laboratoryItemCode;

    /**
     * 检验项目名称
     */
    private String laboratoryItemName;

    /**
     * 标本类型
     */
    private String sampleType;

    /**
     * 检验目的
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
