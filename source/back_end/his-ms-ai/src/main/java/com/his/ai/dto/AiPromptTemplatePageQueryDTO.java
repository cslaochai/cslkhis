package com.his.ai.dto;

import com.his.common.base.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提示词模板分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "提示词模板分页查询入参")
public class AiPromptTemplatePageQueryDTO extends PageParam {

    @Schema(description = "关键词（模板编码/模板名称模糊搜索）")
    private String keyword;
}
