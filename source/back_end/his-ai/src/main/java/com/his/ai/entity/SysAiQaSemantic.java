package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 运营问数语义映射配置（语义词 → 表/列 + 权威口径）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_qa_semantic")
public class SysAiQaSemantic extends BaseEntity {

    /**
     * 语义词/短语（问题文本包含即命中）
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
     * 口径说明（喂给模型作为权威口径）
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
