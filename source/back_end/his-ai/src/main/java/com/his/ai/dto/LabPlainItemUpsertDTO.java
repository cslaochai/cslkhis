package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 检验项目白话词典 · 新增或修改。
 */
@Data
@Schema(description = "白话词典新增或修改入参")
public class LabPlainItemUpsertDTO {

    @Schema(description = "主键，为空表示新增")
    private Long id;

    @Schema(description = "所属分组", example = "血常规")
    private String groupName;

    @NotBlank(message = "检验项目名称不能为空")
    @Schema(description = "检验项目名称，需与 biz_lab_result.laboratory_item_name 精确匹配", example = "血红蛋白")
    private String itemName;

    @Schema(description = "白话名", example = "血色素")
    private String plainName;

    @Schema(description = "这项查什么（患者看的一句话）")
    private String whatIsIt;

    @Schema(description = "结果偏高时的白话说明")
    private String highText;

    @Schema(description = "结果偏低时的白话说明")
    private String lowText;

    @Schema(description = "状态：1-启用 0-停用")
    private Integer status;

    @Schema(description = "排序号，越小越靠前")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
