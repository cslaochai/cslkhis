package com.mis.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 监管规则启停
 */
@Data
public class RuleStatusDTO implements Serializable {

    @NotNull(message = "规则id不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
