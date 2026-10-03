package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 麻精处方限量校验结论（一条 = 一个明确的问题）。
 *
 * <p><b>返回给前端时是"结论即值"，不是布尔 + 让前端猜措辞</b> ——
 * 医生看到的必须是「吗啡注射液：麻醉药品注射剂每张处方限 1 日常用量，本处方 7 日」，
 * 而不是「处方校验失败」。前者医生知道怎么改，后者只会让他反复试。
 */
@Data
public class NarcoticViolationVO {

    /**
     * 级别：BLOCK-拒绝（硬规则，不给放行口子）/ WARN-需补充说明后才放行
     */
    private String level;

    /**
     * 问题码：
     * <ul>
     *   <li>{@code NO_DIAGNOSIS} —— 麻精处方未填写临床诊断</li>
     *   <li>{@code OVER_LIMIT} —— 超过法定处方限量</li>
     *   <li>{@code LIMIT_UNRESOLVED} —— 无法核定处方天数（缺疗程/无法解析频次），
     *       因为「无法证明不超限」在麻精场景下不能按不超限放过</li>
     * </ul>
     */
    private String code;

    /**
     * 可直接展示给医生的问题描述（含具体品种、档位、法定天数、实际天数）
     */
    private String message;

    /**
     * 处方明细ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionDetailId;

    /**
     * 涉及药品名称
     */
    private String drugName;

    /**
     * 该药品的管制分类
     */
    private Integer specialFlag;

    /**
     * 法定允许天数
     */
    private Integer limitDays;

    /**
     * 本处方天数（无法核定时为 null）
     */
    private Integer actualDays;
}
