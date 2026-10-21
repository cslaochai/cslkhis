package com.his.ai.dto;

import com.his.common.base.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运营问数表间关系配置分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "运营问数表间关系配置分页查询入参")
public class OperationQaRelationPageQueryDTO extends PageParam {

    @Schema(description = "源表名模糊搜索")
    private String sourceTable;

    @Schema(description = "目标表名模糊搜索")
    private String targetTable;

    @Schema(description = "关系类型（1-业务关联 2-维度关联）")
    private Integer relationType;

    @Schema(description = "是否启用（1-启用 0-停用）")
    private Integer isEnabled;
}
