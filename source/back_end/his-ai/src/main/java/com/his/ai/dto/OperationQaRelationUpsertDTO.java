package com.his.ai.dto;

import com.his.common.validation.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 运营问数表间关系配置新增/修改入参。
 */
@Data
@Schema(description = "运营问数表间关系配置入参")
public class OperationQaRelationUpsertDTO {

    @Schema(description = "主键（空=新增 有值=修改）")
    private Long id;

    @NotBlank(message = "源表名不能为空")
    @Schema(description = "源表名（主表/业务表）")
    private String sourceTable;

    @NotBlank(message = "源列名不能为空")
    @Schema(description = "源列名")
    private String sourceColumn;

    @NotBlank(message = "目标表名不能为空")
    @Schema(description = "目标表名（维度表/主表）")
    private String targetTable;

    @NotBlank(message = "目标列名不能为空")
    @Schema(description = "目标列名")
    private String targetColumn;

    @NotNull(message = "关系类型不能为空")
    @Schema(description = "关系类型（1-业务关联 2-维度关联）")
    @AllowedValues(values = {"1", "2"}, message = "关系类型不合法")
    private Integer relationType;

    @NotBlank(message = "关系说明不能为空")
    @Schema(description = "关系说明（喂给模型：何时用此关联）")
    private String description;

    @Schema(description = "是否启用（1-启用 0-停用）")
    @AllowedValues(values = {"0", "1"}, message = "是否启用不合法")
    private Integer isEnabled;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
