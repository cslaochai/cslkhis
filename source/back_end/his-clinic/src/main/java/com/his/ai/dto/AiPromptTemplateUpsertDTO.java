package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提示词模板新增版本入参（每次保存生成新版本，不做原地修改）。
 */
@Data
@Schema(description = "提示词模板新增版本入参")
public class AiPromptTemplateUpsertDTO {

    @NotBlank(message = "模板编码不能为空")
    @Schema(description = "模板编码")
    private String templateCode;

    @NotBlank(message = "提示词内容不能为空")
    @Schema(description = "提示词内容")
    private String content;

    @Schema(description = "备注（本次改了什么）")
    private String remark;
}
