package com.his.iot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备共享调配申请入参。
 */
@Data
public class DeviceRentalCreateDTO implements Serializable {

    /** 在院患者（可选；设备科/科室级借用不填） */
    private Long admissionId;
    private String patientName;
    private String bedNo;
    private Long wardId;
    private String wardName;

    @NotBlank(message = "借用原因不能为空")
    private String purpose;

    @NotNull(message = "紧急程度不能为空")
    @AllowedValues(values = {"1", "2", "3"}, message = "紧急程度取值不合法")
    private Integer urgency;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectReturnTime;

    private String deliveryPlace;

    private String remark;

    @Valid
    @NotEmpty(message = "至少选择一台设备")
    private List<DeviceRentalItemDTO> items;
}
