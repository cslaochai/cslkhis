package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 运营问数可查询表（白名单）配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_qa_schema")
public class SysAiQaSchema extends BaseEntity {

    /**
     * 数据表名
     */
    private String tableName;

    /**
     * 表用途说明（喂给模型）
     */
    private String tableDesc;

    /**
     * 是否启用（1-启用 0-停用）
     */
    private Integer isEnabled;

    /**
     * 排序号
     */
    private Integer sortOrder;

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
