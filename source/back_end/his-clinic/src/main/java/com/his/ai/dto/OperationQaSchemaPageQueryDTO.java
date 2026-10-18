package com.his.ai.dto;

import com.his.common.base.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运营问数数据表配置分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "运营问数数据表配置分页查询入参")
public class OperationQaSchemaPageQueryDTO extends PageParam {

    @Schema(description = "表名模糊搜索")
    private String tableName;

    @Schema(description = "是否启用（1-启用 0-停用）")
    private Integer isEnabled;
}
