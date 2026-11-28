package com.his.platform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备档案建档/修改入参（新增与修改同一接口，id 为空走新增）。
 */
@Data
public class EquipmentUpsertDTO implements Serializable {

    /**
     * 设备ID（新增不传）
     */
    private Long id;

    /**
     * 设备编码（资产编号，同一台设备全库唯一；物流机器人的编码同时是接入编号/MQTT 设备号）
     */
    @NotBlank(message = "设备编码不能为空")
    private String equipmentCode;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    private String equipmentName;

    /**
     * 设备类别（字典 his_equipment_category）
     */
    @NotNull(message = "设备类别不能为空")
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6", "7", "8"}, message = "设备类别取值不合法")
    private Integer category;

    /**
     * 使用科室ID
     */
    private Long deptId;

    /**
     * 使用科室名称（随 deptId 由前端带入，档案留快照避免跨模块联查）
     */
    private String deptName;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 型号
     */
    private String model;

    /**
     * 供应商ID（下拉取 /supplier/selectList，全院共用一张供应商档案）
     */
    private Long supplierId;

    /**
     * 供应商名称快照（随 supplierId 由前端带入；档案留快照避免跨模块联查）
     */
    private String supplierName;

    /**
     * 购置日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    /**
     * 购置价格(元)
     */
    @DecimalMin(value = "0", message = "购置价格不能为负数")
    private BigDecimal purchasePrice;

    /**
     * 状态（1-在用 2-停用 3-维修中 4-报废）
     */
    @NotNull(message = "设备状态不能为空")
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "设备状态取值不合法")
    private Integer status;

    /**
     * 是否进入设备共享池（0-否 1-是）；8-物流设备（机器人）不进共享池，由服务端强制为 0
     */
    @AllowedValues(values = {"0", "1"}, message = "共享池标志取值不合法")
    private Integer rentable;

    /**
     * 维保周期(天)
     */
    @Min(value = 1, message = "维保周期至少 1 天")
    @Max(value = 3650, message = "维保周期不能超过 3650 天")
    private Integer maintainCycleDays;

    /**
     * 最近维保日期（新档建档时可带首次维保日期）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastMaintainDate;

    /**
     * 备注
     */
    private String remark;
}
