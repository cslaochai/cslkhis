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
     * 项目来源（1-检验项目 2-检查项目 3-一般检查），决定 item_id 指向哪张项目目录
     */
    private Integer itemSource;

    /**
     * 来源目录项目ID（sys_laboratory_item / sys_inspection_item / sys_treatment_item）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /**
     * 来源目录项目编码（冗余快照，便于报表与结果回写对码）
     */
    private String itemCode;

    /**
     * 项目名称（取目录项目名，冗余快照）
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
