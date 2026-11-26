package com.his.iot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备共享调配审批入参。
 */
@Data
public class DeviceRentalApproveDTO implements Serializable {

    @NotNull(message = "申请单ID不能为空")
    private Long id;

    @NotNull(message = "审批结论不能为空")
    private Boolean approved;

    private String remark;
}
