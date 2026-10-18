package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

/**
 * 药品字典的只读视图（his-ai 侧）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_drug")
public class SysDrugGuide extends BaseEntity {

    /** 药品编码 */
    private String drugCode;

    private String drugName;

    /** 规格 */
    private String specification;

    private String dosageForm;

    /** 单位 */
    private String unit;

    /**
     * 储存条件
     */
    private String storageCondition;

    /**
     * 是否冷链药品（0-否 1-是）
     */
    private Integer isColdChain;

    /**
     * 特殊管理分类（0-普通 1-麻醉 2-一类精神 3-二类精神 4-毒性）
     */
    private Integer specialFlag;

    /**
     * 抗菌药物分级（0-非抗菌 1-非限制 2-限制 3-特殊）
     */
    private Integer antibioticLevel;

    /**
     * 说明书用法用量（成人常规，仅供参考）
     */
    private String usageDosage;
}
