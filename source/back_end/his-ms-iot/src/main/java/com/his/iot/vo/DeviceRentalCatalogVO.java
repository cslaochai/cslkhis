package com.his.iot.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 共享池目录行：设备台账 + 当前可借/总台数。
 */
@Data
public class DeviceRentalCatalogVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String equipmentCode;
    private String equipmentName;
    private Integer category;
    private String deptName;
    private String brand;
    private String model;
    private Integer status;
    private LocalDate lastMaintainDate;

    /** 共享池总台数：有 IoT 设备按 IoT 设备数，没有则按台账 1 台 */
    private Integer totalCount;

    /** 当前可借台数 */
    private Integer availableCount;
}
