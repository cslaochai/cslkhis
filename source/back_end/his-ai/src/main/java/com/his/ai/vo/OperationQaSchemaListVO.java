package com.his.ai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运营问数数据表配置列表项。
 */
@Data
public class OperationQaSchemaListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 数据表名
     */
    private String tableName;

    /**
     * 表用途说明
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
     * 启用列数（喂给模型的列）
     */
    private Integer enabledColumnCount;

    /**
     * 备注
     */
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
