package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备档案实体 —— 对齐既有医疗设备台账表（49-06 铺底，90 行真实档案），本次不改其结构。
 *
 * <p>维保到期口径：最后维保日期 + 维保周期天数 = 下次维保日期（VO 里现算，不落库）。
 */
@Data
@TableName("sys_equipment")
public class SysEquipment {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
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
