package com.his.ai.dto;

import com.his.common.base.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验项目白话词典 · 后台检索条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "白话词典检索条件")
public class LabPlainItemSearchDTO extends PageParam {

    @Schema(description = "分组名，为空查全部")
    private String groupName;

    @Schema(description = "关键词，匹配检验项目名 / 白话名")
    private String keyword;

    @Schema(description = "状态：1-启用 0-停用，为空查全部")
    private Integer status;
}
