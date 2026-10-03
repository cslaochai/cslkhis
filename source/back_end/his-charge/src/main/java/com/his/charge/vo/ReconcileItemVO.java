package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 三级对账的**一行**结论。
 *
 * <p>设计成"左口径 vs 右口径"的通用形状，而不是给每一级写一个字段：
 * 三级对账本质都是"两个独立来源的数对不上吗"，统一形状才能让页面用同一个组件渲染，
 * 也让新增一级对账（比如将来加"渠道对账"）不用改前端。
 */
@Data
public class ReconcileItemVO {

    /**
     * 级次标识（cashierShift / dayVsShift / dayVsDetail / deptAttribution）
     */
    private String level;

    /**
     * 级次名称（人读）
     */
    private String levelName;

    /**
     * 左侧口径说明
     */
    private String leftLabel;

    /**
     * 左侧金额
     */
    private BigDecimal leftAmount;

    /**
     * 右侧口径说明
     */
    private String rightLabel;

    /**
     * 右侧金额
     */
    private BigDecimal rightAmount;

    /**
     * 差额 = 右 - 左
     */
    private BigDecimal diffAmount;

    /**
     * 是否平（差额为 0）
     */
    private Boolean passed;

    /**
     * 差异笔数（能定位到笔数时填，否则 null）
     */
    private Integer diffCount;

    /**
     * 结论说明（不平的时候必须写清"差在哪"，不能只说一句"对不上"）
     */
    private String message;
}
