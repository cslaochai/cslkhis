package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 体检套餐项目明细实体（体检套餐项目）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_checkup_package_item")
public class SysCheckupPackageItem extends BaseEntity {

    /**
     * 套餐ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long packageId;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 项目类别（问诊/体格）（1-检验 2-检查 3-一般）
     */
    private Integer itemType;

    /**
     * 参考范围/标准
     */
    private String refStandard;

    /**
     * 单项金额（元）
     */
    private BigDecimal amount;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
