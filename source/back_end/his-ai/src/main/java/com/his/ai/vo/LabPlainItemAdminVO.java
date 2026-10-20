package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 检验项目白话词典 · 后台列表行。
 */
@Data
@Schema(description = "白话词典维护行")
public class LabPlainItemAdminVO {

    /** 词典条目ID */
    @Schema(description = "主键（19 位雪花 ID，前端全程按字符串处理，按数字会丢精度）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 所属分组 */
    @Schema(description = "所属分组")
    private String groupName;

    /** 检验项目名称 */
    @Schema(description = "检验项目名称")
    private String itemName;

    /** 白话名 */
    @Schema(description = "白话名")
    private String plainName;

    /** 这项检查查什么（白话说明） */
    @Schema(description = "这项查什么")
    private String whatIsIt;

    /** 结果偏高的白话说明 */
    @Schema(description = "偏高说明")
    private String highText;

    /** 结果偏低的白话说明 */
    @Schema(description = "偏低说明")
    private String lowText;

    /** 状态（0-停用 1-启用） */
    @Schema(description = "状态：1-启用 0-停用")
    private Integer status;

    /** 排序号 */
    @Schema(description = "排序号")
    private Integer sortOrder;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}
