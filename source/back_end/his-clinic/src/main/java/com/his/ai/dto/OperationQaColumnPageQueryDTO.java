package com.his.ai.dto;

import com.his.common.base.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运营问数数据列配置分页查询入参（按表查）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "运营问数数据列配置分页查询入参")
public class OperationQaColumnPageQueryDTO extends PageParam {

    @NotBlank(message = "数据表名不能为空")
    @Schema(description = "数据表名")
    private String tableName;

    @Schema(description = "是否喂给模型（1-启用 0-停用）")
    private Integer isEnabled;
}
