package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 运营问数数据列配置列表项。
 */
@Data
public class OperationQaColumnListVO {

    /** 数据列配置记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 数据表名
     */
    private String tableName;

    /**
     * 列名
     */
    private String columnName;

    /**
     * 列含义说明
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
}
