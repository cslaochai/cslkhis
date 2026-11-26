package com.his.iot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备共享调配申请明细入参：一行一台设备。
 */
@Data
public class DeviceRentalItemDTO implements Serializable {

    @NotNull(message = "设备台账ID不能为空")
    private Long equipmentId;

    /** 期望品牌（可空） */
    private String requestedBrand;

    /** 期望型号（可空） */
    private String requestedModel;

    private String remark;
}
