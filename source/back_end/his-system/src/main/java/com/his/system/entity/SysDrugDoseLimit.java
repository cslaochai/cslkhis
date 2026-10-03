package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 药品剂量上限知识条目（一行一个成分）
 * <p>
 * 命中只出提示、不拦审方：同一成分在不同人群（老年/肾功能不全/儿科）极量不同，
 * 系统拿不到体重与肌酐清除率，不能代替人判断。可比口径的边界见 {@code sql/130} 头注第四条
 * —— 只支持 g/mg/ug，IU 类与按周给药的药物一律不铺本表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_drug_dose_limit")
public class SysDrugDoseLimit extends BaseEntity {

    /** 成分关键字（命中口径同 SysDrugInteraction.componentA） */
    private String component;

    /** 剂量单位（字典 his_dose_unit：g/mg/ug） */
    private String doseUnit;

    /** 单次最大量（NULL=本项不判） */
    private BigDecimal maxSingleDose;

    /** 每日最大量（NULL=本项不判） */
    private BigDecimal maxDailyDose;

    /** 口径说明（按什么人群/剂型定的极量，界面直接显示） */
    private String note;

    /** 状态（1-启用 0-停用） */
    private Integer status;
}
