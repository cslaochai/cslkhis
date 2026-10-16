package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 治疗项目字典。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_treatment_item")
public class SysTreatmentItem extends BaseEntity implements Serializable {

    /**
     * 项目编码（唯一）
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 项目类型（1-注射 2-输液 3-换药 4-拆线 5-其他）
     */
    private Integer itemType;

    /**
     * 执行科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 治疗价格
     */
    private BigDecimal price;

    /**
     * 治疗时长（分钟）
     */
    private Integer duration;

    /**
     * 使用方法
     */
    private String usageMethod;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

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
