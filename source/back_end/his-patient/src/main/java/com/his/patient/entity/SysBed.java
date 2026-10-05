package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 床位
 * <p>既有表，主键床位ID，不继承 BaseEntity。
 * <p><b>床位占用状态以本表为准</b>：病区.occupied_beds 与 {@code total_beds} 是演示数据
 * （实测 1 号病区写 30 床/占用 22，而本表该病区只有 4 条床位），禁止作为判断依据。
 */
@Data
@TableName("sys_bed")
public class SysBed implements Serializable {

    /**
     * 床位ID
     */
    @TableId(value = "bed_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /**
     * 床位号
     */
    private String bedNo;

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 床位类型：normal-普通 ICU-重症 VIP-特需
     */
    private String bedType;

    /**
     * 床位状态（0-维修 1-空闲 2-占用 3-锁定）
     */
    private Integer bedStatus;

    /**
     * 当前占用患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
