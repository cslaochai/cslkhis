package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * CMI 样本：近 30 日出院且主诊断已编码的病案首页一行，带齐分组器要用的病案维度。
 */
@Data
public class BiCodedSummaryRowVO implements Serializable {

    /**
     * 主要诊断ICD 编码
     */
    private String icdCode;

    /**
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄数字，配合 ageUnit
     */
    private Integer age;

    /**
     * 年龄单位（1-岁 2-月 3-天）
     */
    private Integer ageUnit;

    /**
     * 出生体重（克）
     */
    private Integer admissionWeightG;

    /**
     * 主要手术操作编码（逗号拼接）
     */
    private String mainOperCodes;

    /**
     * 其他手术操作编码（逗号拼接）
     */
    private String otherOperCodes;

    /**
     * 其他诊断编码（逗号拼接）
     */
    private String otherDiagCodes;
}
