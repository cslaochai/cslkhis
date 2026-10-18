package com.his.patient.vo;

import lombok.Data;

/**
 * 结构化过敏史（读侧契约）。
 */
@Data
public class AllergyFactVO {

    /** 过敏类型（药物/食物/其他） */
    private Integer allergyType;

    /** 过敏原名称 */
    private String allergenName;

    /** 过敏严重程度（轻度/中度/重度/危及生命） */
    private Integer allergySeverity;
}
