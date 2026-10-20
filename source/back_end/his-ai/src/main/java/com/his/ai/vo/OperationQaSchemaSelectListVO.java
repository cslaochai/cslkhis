package com.his.ai.vo;

import lombok.Data;

/**
 * 运营问数数据表下拉选项（配置弹框选表用）。
 */
@Data
public class OperationQaSchemaSelectListVO {

    /**
     * 数据表名
     */
    private String tableName;

    /**
     * 表用途说明
     */
    private String tableDesc;
}
