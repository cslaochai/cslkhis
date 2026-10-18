package com.his.ai.dto;

import com.his.common.base.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运营问数语义映射配置分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "运营问数语义映射配置分页查询入参")
public class OperationQaSemanticPageQueryDTO extends PageParam {

    @Schema(description = "语义词模糊搜索")
    private String term;

    @Schema(description = "命中表")
    private String tableName;

    @Schema(description = "是否启用（1-启用 0-停用）")
    private Integer isEnabled;
}
