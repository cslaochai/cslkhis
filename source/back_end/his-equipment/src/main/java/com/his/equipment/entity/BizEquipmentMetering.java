package com.his.equipment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备计量记录实体（86 号脚本新增）。
 *
 * <p>计量类型：1-强检 2-校准。有效期至（valid_until）是强检合规的判定口径，过期即台账亮红。
 */
@Data
@TableName("biz_equipment_metering")
public class BizEquipmentMetering {

    /**
     * 计量记录ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 设备ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long equipmentId;
    /**
     * 设备编码（快照）
     */
    private String equipmentCode;
    /**
     * 设备名称（快照）
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
}
