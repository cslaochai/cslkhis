package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 运营问数表间关系配置列表项。
 */
@Data
public class OperationQaRelationListVO {

    /** 表间关系配置记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 源表名（主表/业务表）
     */
    private String sourceTable;

    /**
     * 源列名
     */
    private String sourceColumn;

    /**
     * 目标表名（维度表/主表）
     */
    private String targetTable;

    /**
     * 目标列名
     */
    private String targetColumn;

    /**
     * 关系类型（1-业务关联 2-维度关联）
     */
    private Integer relationType;

    /**
     * 关系说明
     */
    private String description;

    /**
     * 是否启用（1-启用 0-停用）
     */
    private Integer isEnabled;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
