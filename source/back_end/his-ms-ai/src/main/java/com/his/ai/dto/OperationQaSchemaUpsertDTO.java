package com.his.ai.dto;

import com.his.common.validation.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 运营问数数据表配置新增/修改入参。
 */
@Data
@Schema(description = "运营问数数据表配置入参")
public class OperationQaSchemaUpsertDTO {

    @Schema(description = "主键（空=新增 有值=修改）")
    private Long id;

    /** 数据表名 */
    @NotBlank(message = "数据表名不能为空")
    @Schema(description = "数据表名")
    private String tableName;

    @NotBlank(message = "表用途说明不能为空")
    @Schema(description = "表用途说明（喂给模型）")
    private String tableDesc;

    @Schema(description = "是否启用（1-启用 0-停用）")
    @AllowedValues(values = {"0", "1"}, message = "是否启用不合法")
    private Integer isEnabled;

    @Schema(description = "排序号")
    private Integer sortOrder;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}
