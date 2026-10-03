package com.his.emr.dto;

import lombok.Data;

/**
 * 检查申请模板新增入参
 */
@Data
public class BizInspectionTemplateUpsertDTO {
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
     * 检查项目ID
     */
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
     * 是否急诊：0-否 1-是
     */
    private Integer isEmergency;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;
}
