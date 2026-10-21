package com.his.clinical.vo;

import lombok.Data;

import java.io.Serializable;

/** 质控维度下拉项：质控规则配置与筛选下拉用的维度编码、名称与判定口径。 */
@Data
public class QcDimensionSelectListVO implements Serializable {

    /** 维度码值（1-完整性 2-规范性 3-逻辑性） */
    private Integer code;

    /** 维度名称（下拉展示文案） */
    private String text;

    /** 维度判定口径说明 */
    private String description;
}
