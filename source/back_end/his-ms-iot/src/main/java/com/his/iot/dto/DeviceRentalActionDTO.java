package com.his.iot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备共享调配单动作入参（发放/验收）。
 */
@Data
public class DeviceRentalActionDTO implements Serializable {

    @NotNull(message = "申请单ID不能为空")
    private Long id;

    private String remark;
}
