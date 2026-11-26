package com.his.iot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备归还申请入参。
 */
@Data
public class DeviceRentalReturnDTO implements Serializable {

    @NotNull(message = "申请单ID不能为空")
    private Long id;

    @NotBlank(message = "归还原因不能为空")
    private String reason;
}
