package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 抗菌药物品名别名 —— 住院医嘱名 → 药品目录的<b>精确</b>匹配键。
 *
 * <p>为什么需要它：住院医嘱住院医嘱主表只有 item_code / item_name，
 * 没有 drug_id；且医嘱写法（"注射用头孢曲松钠"）与药品目录名（"头孢曲松钠粉针"）不一致。
 * 靠 LIKE 模糊匹配会把"头孢XX"全算成一种药，使用强度就算废了。
 *
 * <p>匹配顺序（{@code AntibioticStatsMapper}）：drug_code → 本表 alias_name → drug_name / generic_name。
 * 三级都命中不了就不计入（宁可漏算，不可错算），漏的条数进 unmatched_order_count 提示维护。
 *
 * <p>无 del_flag，删除走物理删（uk_antibiotic_alias 不含 del_flag）。
 */
@Data
@TableName("biz_antibiotic_alias")
public class BizAntibioticAlias implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 药品ID（药品字典的ID） */
    private Long drugId;

    /** 药品编码（快照） */
    private String drugCode;

    /** 药品目录名（快照） */
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
