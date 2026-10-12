package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 运营问数语义映射配置新增/修改入参。
 */
@Data
@Schema(description = "运营问数语义映射配置入参")
public class OperationQaSemanticUpsertDTO {

    @Schema(description = "主键（空=新增 有值=修改）")
    private Long id;

    @NotBlank(message = "语义词不能为空")
    @Schema(description = "语义词/短语（问题文本包含即命中）")
    private String term;

    @NotBlank(message = "命中表不能为空")
    @Schema(description = "命中表")
    private String tableName;

    @Schema(description = "目标列（空=表级口径）")
    private String columnName;

    @NotBlank(message = "口径说明不能为空")
    @Schema(description = "口径说明（喂给模型作为权威口径）")
    private String scopeDesc;

    @NotNull(message = "匹配方式不能为空")
    @Schema(description = "匹配方式（1-包含 2-精确）")
    private Integer matchMode;

    @Schema(description = "优先级（大者先）")
    private Integer priority;

    @Schema(description = "是否启用（1-启用 0-停用）")
    private Integer isEnabled;

    @Schema(description = "备注")
    private String remark;
}
