package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 设备科共享池目录行：只读设备台账，供设备共享调配页选设备。
 */
@Data
public class EquipmentRentalCatalogVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 设备编码/资产编号 */
    private String equipmentCode;

    /** 设备名称 */
    private String equipmentName;

    /** 设备类别（字典 his_equipment_category） */
    private Integer category;

    /** 使用科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 使用科室名称 */
    private String deptName;

    private String brand;

    private String model;

    /** 设备状态（1-在用 2-停用 3-维修中 4-报废） */
    private Integer status;

    /** 是否进入共享池 */
    private Integer rentable;

    /** 最近保养日期 */
    private LocalDate lastMaintainDate;
}
