package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 检查项目字典
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_inspection_item")
public class SysInspectionItem extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 项目编码（唯一）
     */
    private String itemCode;
    /**
     * 项目名称
     */
    private String itemName;
    /**
     * 项目类型（1-放射检查 2-超声检查 3-心电图 4-内镜检查 5-其他）
     */
    private Integer itemType;

    /**
     * 检查科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 检查部位
     */
    private String bodyPart;
    /**
     * 检查价格
     */
    private BigDecimal price;
    /**
     * 检查时长（分钟）
     */
    private Integer duration;
    /**
     * 检查前准备
     */
    private String preparation;
    /**
     * 检查禁忌
     */
    private String contraindication;
    /**
     * 是否支持急诊（0-否 1-是）
     */
    private Integer isEmergency;
    /**
     * 是否需要预约（0-否 1-是）
     */
    private Integer isAppointment;
    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
