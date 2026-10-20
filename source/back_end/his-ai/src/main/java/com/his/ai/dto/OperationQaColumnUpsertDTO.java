package com.his.ai.dto;

import com.his.common.validation.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 运营问数数据列配置新增/修改入参。
 */
@Data
@Schema(description = "运营问数数据列配置入参")
public class OperationQaColumnUpsertDTO {

    @Schema(description = "主键（空=新增 有值=修改）")
    private Long id;

    /** 数据表名 */
    @NotBlank(message = "数据表名不能为空")
    @Schema(description = "数据表名")
    private String tableName;

    @NotBlank(message = "列名不能为空")
    @Schema(description = "列名")
    private String columnName;

    @NotBlank(message = "列含义说明不能为空")
    @Schema(description = "列含义说明（来自表结构注释）")
    private String columnDesc;

    @Schema(description = "可否用于过滤条件（1-是 0-否）")
    @AllowedValues(values = {"0", "1"}, message = "可否用于过滤条件不合法")
    private Integer isFilterable;

    @Schema(description = "可否用于分组统计（1-是 0-否）")
    @AllowedValues(values = {"0", "1"}, message = "可否用于分组统计不合法")
    private Integer isGroupable;

    @Schema(description = "是否喂给模型（1-启用 0-停用）")
    @AllowedValues(values = {"0", "1"}, message = "是否喂给模型不合法")
    private Integer isEnabled;

    @Schema(description = "敏感类型（0-普通 1-脱敏 2-禁止出库）")
    @NotNull(message = "请选择敏感类型")
    @AllowedValues(values = {"0", "1", "2"}, message = "敏感类型不合法")
    private Integer sensitiveType;

    /** 脱敏方式（phone-手机号 idcard-身份证 email-邮箱 middle-姓名/中间打码） */
    @Schema(description = "脱敏方式（phone-手机号 idcard-身份证 email-邮箱 middle-姓名/中间打码）")
    @AllowedValues(values = {"phone", "idcard", "email", "middle"}, message = "脱敏方式不合法")
    private String maskType;

    @Schema(description = "排序号")
    private Integer sortOrder;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}
