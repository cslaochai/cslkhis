package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 体检套餐实体（对齐 87 号建表体检套餐）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_checkup_package")
public class SysCheckupPackage extends BaseEntity {

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 套餐编码
     */
    private String packageCode;

    /**
     * 适用性别:0-不限 1-男 2-女（性别字典口径）
     */
    private Integer genderLimit;

    /**
     * 套餐价格（元）
     */
    private BigDecimal price;

    /**
     * 套餐说明
     */
    private String description;

    /**
     * his_enable_status:0-禁用 1-启用
     */
    private Integer status;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
