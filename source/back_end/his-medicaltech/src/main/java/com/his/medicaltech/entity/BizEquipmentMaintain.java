package com.his.medicaltech.entity;

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
 * 设备维保记录实体（86 号脚本新增）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_equipment_maintain")
public class BizEquipmentMaintain extends BaseEntity {

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
     * 维保类型（1-保养 2-维修 3-巡检）
     */
    private Integer maintainType;
    /**
     * 维保日期
     */
    private LocalDate maintainDate;
    /**
     * 下次维保日期
     */
    private LocalDate nextMaintainDate;
    /**
     * 费用（元）
     */
    private BigDecimal cost;
    /**
     * 故障描述
     */
    private String faultDesc;
    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 维保结果（1-正常 2-异常）
     */
    private Integer maintainResult;

    /**
     * 维保人
     */
    private String handlerName;
    /**
     * 删除标志（0-正常 1-删除）
    @TableField(fill = FieldFill.INSERT)
     */
    private Integer delFlag;
}
