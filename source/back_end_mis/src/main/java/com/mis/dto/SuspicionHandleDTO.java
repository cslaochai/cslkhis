package com.mis.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 疑点处置（确认违规 / 排除）
 */
@Data
public class SuspicionHandleDTO implements Serializable {

    @NotNull(message = "疑点id不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 确认违规金额 */
    private BigDecimal amount;

    /** 备注 */
    private String remark;
}
