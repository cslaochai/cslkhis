package com.his.ai.vo;

import lombok.Data;

/**
 * 运营问数字段下拉选项（关系/语义配置弹框按表选列用）。
 */
@Data
public class OperationQaColumnSelectListVO {

    /**
     * 字段名
     */
    private String columnName;

    /**
     * 字段说明
     */
    private String columnDesc;

    /**
     * 字段类型
     */
    private String columnType;
}
