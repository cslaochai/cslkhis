package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 检验项目白话词典 · 后台检索条件。
 *
 * <p>与患者端无关，这里要能查出<b>停用</b>的条目（运营要靠它把停用的改回来）。
 */
@Data
@Schema(description = "白话词典检索条件")
public class LabPlainItemSearchDTO {

    @Schema(description = "页码，从 1 开始")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 20;

    @Schema(description = "分组名，为空查全部")
    private String groupName;

    @Schema(description = "关键词，匹配检验项目名 / 白话名")
    private String keyword;

    @Schema(description = "状态：1-启用 0-停用，为空查全部")
    private Integer status;
}
