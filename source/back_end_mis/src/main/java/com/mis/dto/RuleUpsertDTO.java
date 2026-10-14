package com.mis.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 监管规则新增/修改
 */
@Data
public class RuleUpsertDTO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @NotBlank(message = "规则编码不能为空")
    private String ruleCode;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    @NotNull(message = "稽核对象不能为空")
    private Integer target;

    @NotNull(message = "违规级别不能为空")
    private Integer severity;

    /** 规则参数（JSON） */
    private String params;

    private String description;

    /** 状态（1-启用 0-停用） */
    private Integer status;
}
