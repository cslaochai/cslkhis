package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 检验项目白话词典 · 后台列表行。
 *
 * <p>比患者端多两个字段：{@code status}（运营要看停用态）、{@code sortOrder}（排序要能改）。
 */
@Data
@Schema(description = "白话词典维护行")
public class LabPlainItemAdminVO {

    @Schema(description = "主键（19 位雪花 ID，前端全程按字符串处理，按数字会丢精度）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "所属分组")
    private String groupName;

    @Schema(description = "检验项目名称")
    private String itemName;

    @Schema(description = "白话名")
    private String plainName;

    @Schema(description = "这项查什么")
    private String whatIsIt;

    @Schema(description = "偏高说明")
    private String highText;

    @Schema(description = "偏低说明")
    private String lowText;

    @Schema(description = "状态：1-启用 0-停用")
    private Integer status;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
