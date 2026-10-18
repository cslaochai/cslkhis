package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 运营问数语义映射配置列表项。
 */
@Data
public class OperationQaSemanticListVO {

    /** 语义映射配置记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 语义词/短语
     */
    private String term;

    /**
     * 命中表
     */
    private String tableName;

    /**
     * 目标列（空=表级口径）
     */
    private String columnName;

    /**
     * 口径说明
     */
    private String scopeDesc;

    /**
     * 匹配方式（1-包含 2-精确）
     */
    private Integer matchMode;

    /**
     * 优先级（大者先）
     */
    private Integer priority;

    /**
     * 是否启用（1-启用 0-停用）
     */
    private Integer isEnabled;
}
