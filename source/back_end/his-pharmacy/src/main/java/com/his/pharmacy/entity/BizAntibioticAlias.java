package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 抗菌药物品名别名 —— 住院医嘱名 → 药品目录的精确匹配键。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_antibiotic_alias")
public class BizAntibioticAlias extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */

    /** 药品ID（药品字典的ID） */
    private Long drugId;

    /** 药品编码 */
    private String drugCode;

    /** 药品目录名 */
    private String drugName;

    /** 别名（医嘱/处方里出现的名称，精确匹配） */
    private String aliasName;

    /** 创建人 */

    /** 创建时间 */

    /** 更新时间 */

    /** 备注 */
    private String remark;
}
