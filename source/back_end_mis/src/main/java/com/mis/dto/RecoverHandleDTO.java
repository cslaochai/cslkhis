package com.mis.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 追回单处置（确认追回 / 核销）
 */
@Data
public class RecoverHandleDTO implements Serializable {

    @NotNull(message = "追回单id不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 备注 */
    private String remark;
}
