package com.his.supplies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 耗材字典（耗材字典是耗材域唯一目录，库存只挂 consumable_id）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_consumable")
public class SysConsumable extends BaseEntity {
    /**
     * 耗材编码（唯一）
     */
    private String consumableCode;

    /**
     * 耗材名称
     */
    private String consumableName;

    /**
     * 类别（1-卫生材料 2-注射穿刺 3-医用敷料 4-防护用品 5-其他）
     */
    private Integer category;

    /**
     * 规格
     */
    private String specification;

    /**
     * 单位（包、支、盒、个等）
     */
    private String unit;

    /**
     * 生产厂家
     */
    private String manufacturer;

    /**
     * 零售价
     */
    private BigDecimal retailPrice;

    /**
     * 是否高值耗材（0-普通 1-高值：必须UDI扫码使用登记）
     */
    private Integer isHighValue;

    /**
     * 产品级UDI-DI（GS1 (01) 段，扫码匹配键）
     */
    private String udiDi;

    /**
     * 医疗器械注册证/备案号
     */
    private String regCertNo;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
