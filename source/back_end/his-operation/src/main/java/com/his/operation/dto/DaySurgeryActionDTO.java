package com.his.operation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术动作入参（取消；原因必填）。
 */
@Data
public class DaySurgeryActionDTO implements Serializable {

    @NotNull(message = "登记单ID不能为空")
    private Long id;

    /** 取消原因（必填） */
    @NotBlank(message = "取消原因必填")
    private String content;
}
