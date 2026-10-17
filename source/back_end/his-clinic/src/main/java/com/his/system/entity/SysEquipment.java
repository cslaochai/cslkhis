package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备档案实体 —— 对齐既有医疗设备台账表（49-06 铺底，90 行真实档案），本次不改其结构。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_equipment")
public class SysEquipment extends BaseEntity {

    /**
     * 设备编码
     */
    private String equipmentCode;
    /**
     * 设备名称
     */
    private String equipmentName;

    /**
     * 设备类别（字典 his_equipment_category，1~7）
     */
    private Integer category;

    /**
     * 使用科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 使用科室名称
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
     * 购置日期
     */
    private LocalDate purchaseDate;
    /**
     * 购置价格(元)
     */
    private BigDecimal purchasePrice;

    /**
     * 状态（字典 his_equipment_status）:1-在用 2-停用 3-维修中 4-报废
     */
    private Integer status;

    /**
     * 维保周期(天)
     */
    private Integer maintainCycleDays;
    /**
     * 最近维保日期
     */
    private LocalDate lastMaintainDate;

    /**
     * 删除标志（0-正常 1-删除）
    @TableField(fill = FieldFill.INSERT)
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
