package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 药品字典的只读视图（his-ai 侧）。
 *
 * <p><b>为什么另建一个而不是复用 his-pharmacy 的实体：</b>用药说明只需要这几列，
 * 而 his-ai 不依赖 his-pharmacy 模块。跨模块读表在本仓的既定做法是
 * 「读侧自建只读实体 + 自己模块的 Mapper」，不是把对方模块拉进来。
 * <b>这个实体只用于读，不要拿它做写操作。</b>
 */
@Data
@TableName("sys_drug")
public class SysDrugGuide {

    @TableId(type = IdType.NONE)
    private Long id;

    private String drugCode;

    private String drugName;

    private String specification;

    private String dosageForm;

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
