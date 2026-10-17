package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 运营问数数据列配置（列含义来自真实表结构注释）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_qa_column")
public class SysAiQaColumn extends BaseEntity {

    /**
     * 数据表名
     */
    private String tableName;

    /**
     * 列名
     */
    private String columnName;

    /**
     * 列含义说明（来自表结构注释）
     */
    private String columnDesc;

    /**
     * 列类型（来自 information_schema data_type，重写刷新写入）
     */
    private String columnType;

    /**
     * 可否用于过滤条件（1-是 0-否）
     */
    private Integer isFilterable;

    /**
     * 可否用于分组统计（1-是 0-否）
     */
    private Integer isGroupable;

    /**
     * 是否喂给模型（1-启用 0-停用）
     */
    private Integer isEnabled;

    /**
     * 敏感类型（0-普通 1-脱敏 2-禁止出库）
     */
    private Integer sensitiveType;

    /**
     * 脱敏方式（phone-手机号 idcard-身份证 email-邮箱 middle-姓名/中间打码）
     */
    private String maskType;

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
