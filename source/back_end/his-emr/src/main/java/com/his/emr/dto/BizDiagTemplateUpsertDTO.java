package com.his.emr.dto;

import lombok.Data;

/**
 * 常用诊断模板保存入参
 */
@Data
public class BizDiagTemplateUpsertDTO {
    /**
     * 模板ID，新增时为空
     */
    private Long id;

    /**
     * 所属医生ID（后端以当前登录用户覆盖）
     */
    private Long doctorId;

    /**
     * ICD-10 诊断编码
     */
    private String icdCode;

    /**
     * ICD-10 诊断名称
     */
    private String icdName;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;
}
