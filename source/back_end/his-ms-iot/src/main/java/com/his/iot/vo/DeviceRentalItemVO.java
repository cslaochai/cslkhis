package com.his.iot.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备共享调配申请明细 VO。
 */
@Data
public class DeviceRentalItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;
    private String requestedBrand;
    private String requestedModel;

    private String deviceNo;
    private String deviceType;

    private Integer status;
    private String statusText;

    private LocalDateTime issueTime;
    private LocalDateTime returnTime;
    private LocalDateTime acceptTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long bindId;

    private String remark;
}
