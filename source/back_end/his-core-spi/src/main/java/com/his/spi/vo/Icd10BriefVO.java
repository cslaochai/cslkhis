package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ICD-10 编码摘要：跨模块做编码检索与码表校验的出参。
 */
@Data
@NoArgsConstructor
public class Icd10BriefVO {

    /**
     * ICD-10 编码
     */
    private String icdCode;

    /**
     * 诊断名称
     */
    private String icdName;

    /**
     * 编码分类
     */
    private String icdCategory;

    /**
     * 维护顺序号（越小越靠前）
     */
    private Integer sortOrder;
}
