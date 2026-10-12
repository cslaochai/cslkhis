package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 日间手术准入目录（术式准入 + 最长滞留小时数）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_day_surgery_item")
public class BizDaySurgeryItem extends BaseEntity {

    /**
     * 术式编码
     */
    private String itemCode;

    /**
     * 术式名称
     */
    private String itemName;

    /**
     * 适用科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 适用科室名称
     */
    private String deptName;

    /**
     * 最长滞留小时数（默认 48，超时判超期）
     */
    private Integer maxStayHours;

    /**
     * 手术级别（1~4）
     */
    private Integer operationLevel;

    /**
     * 麻醉方式（1-局部麻醉 2-椎管内麻醉 3-全身麻醉 4-神经阻滞 5-其他）
     */
    private Integer anesthesiaType;

    /**
     * 标准费用
     */
    private BigDecimal standardFee;

    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
