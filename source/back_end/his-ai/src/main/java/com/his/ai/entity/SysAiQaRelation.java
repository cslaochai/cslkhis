package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 运营问数表间关系配置（join 权威键：源表.列 = 目标表.列）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_qa_relation")
public class SysAiQaRelation extends BaseEntity {

    /**
     * 源表名
     */
    private String sourceTable;

    /**
     * 源表关联列
     */
    private String sourceColumn;

    /**
     * 目标表名
     */
    private String targetTable;

    /**
     * 目标表关联列
     */
    private String targetColumn;

    /**
     * 关系类型（1-业务关联 2-维度关联）
     */
    private Integer relationType;

    /**
     * 关系说明（喂给模型：何时用此关联）
     */
    private String description;

    /**
     * 是否启用（1-启用 0-停用）
     */
    private Integer isEnabled;

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
