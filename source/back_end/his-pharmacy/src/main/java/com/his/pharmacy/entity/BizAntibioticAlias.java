package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 抗菌药物品名别名 —— 住院医嘱名 → 药品目录的精确匹配键。
 */
@Data
@TableName("biz_antibiotic_alias")
public class BizAntibioticAlias implements Serializable {
    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 药品ID（药品字典的ID） */
    private Long drugId;

    /** 药品编码 */
    private String drugCode;

    /** 药品目录名 */
    private String drugName;

    /** 别名（医嘱/处方里出现的名称，精确匹配） */
    private String aliasName;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;
}
