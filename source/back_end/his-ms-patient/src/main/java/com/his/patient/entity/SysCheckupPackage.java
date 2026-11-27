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
     * 原价（元）：保存时明细项目价格合计的快照。目录调价后仍保留当时的价格基线
     */
    private BigDecimal listPrice;

    /**
     * 套餐折扣率（套餐价/原价，1.0000 表示不打折）
     */
    private BigDecimal discountRate;

    /**
     * 最低折扣率（价格底线）：套餐价不得低于「原价 × 本值」
     */
    private BigDecimal floorRate;

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
