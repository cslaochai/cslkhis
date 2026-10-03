package com.his.emr.dto;

import lombok.Data;

/**
 * 检验申请模板新增入参
 */
@Data
public class BizLaboratoryTemplateUpsertDTO {
    /**
     * 模板ID，新增时为空
     */
    private Long id;

    /**
     * 所属医生ID（后端以当前登录用户覆盖）
     */
    private Long doctorId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 检验项目ID
     */
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
     * 标本类型（如静脉血、尿液）
     */
    private String sampleType;

    /**
     * 检验目的
     */
    private String inspectionPurpose;

    /**
     * 是否急诊：0-否 1-是
     */
    private Integer isEmergency;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;
}
