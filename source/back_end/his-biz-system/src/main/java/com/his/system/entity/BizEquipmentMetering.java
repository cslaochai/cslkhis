package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.time.LocalDate;

/**
 * 设备计量记录实体（86 号脚本新增）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_equipment_metering")
public class BizEquipmentMetering extends BaseEntity {

    /**
     * 设备ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long equipmentId;
    /**
     * 设备编码
     */
    private String equipmentCode;
    /**
     * 设备名称
     */
    private String equipmentName;

    /**
     * 计量类型（1-强检 2-校准）
     */
    private Integer meteringType;
    /**
     * 计量日期
     */
    private LocalDate meteringDate;

    /**
     * 有效期至
     */
    private LocalDate validUntil;

    /**
     * 计量结果（1-合格 2-不合格）
     */
    private Integer meteringResult;

    /**
     * 证书编号
     */
    private String certNo;
    /**
     * 检定/校准机构
     */
    private String agency;
    /**
     * 删除标志（0-正常 1-删除）
    @TableField(fill = FieldFill.INSERT)
     */
    private Integer delFlag;
}
