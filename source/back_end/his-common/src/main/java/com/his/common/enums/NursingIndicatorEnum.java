package com.his.common.enums;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 护理质量指标枚举（sql/168）
 *
 * <p>护理部要的四个数分两类，**分母来源完全不同**，所以不能塞进一个「合格率」列里：
 * <ul>
 *   <li>合格率类（{@code multiplier=100}）：分母是抽查例数，事实来自护理质量检查单；</li>
 *   <li>千床日率类（{@code multiplier=1000}）：分母是实际占用床日数，事实来自
 *       不良事件上报（分子）+ 入院记录（分母），
 *       压疮只算 {@link AdverseAcquiredEnum#HOSPITAL_ACQUIRED}，入院带入的压疮计入留档但不计入发生率。</li>
 * </ul>
 *
 * <p>千床日类<b>故意不设目标值</b>：院内发生率的目标要按床位类型、收治结构分级定标
 * （三级医院评审看的是趋势与同比，不是一个拍出来的常数），台账 {@code target_value} 留空，
 * {@code reached_flag} 随之为 NULL，页面上显示「—」而不是「未达标」。
 * <br>字典权威在本枚举，码值改动必须同步 {@code sql/168} 的 {@code his_nursing_indicator} 段。
 */
@Getter
public enum NursingIndicatorEnum {

    /**
     * 基础护理合格率（检查类别 1）
     */
    BASIC_NURSING("BASIC_NURSING", "基础护理合格率", "%", 100, "90", 1, 1, null),
    /**
     * 护理文书书写合格率（检查类别 4）
     */
    NURSING_DOC("NURSING_DOC", "护理文书书写合格率", "%", 100, "95", 1, 4, null),
    /**
     * 跌倒/坠床发生率（不良事件 event_type=2）
     */
    FALL_RATE("FALL_RATE", "跌倒/坠床发生率", "例/千床日", 1000, null, 2, null, 2),
    /**
     * 院内压力性损伤发生率（不良事件 event_type=3 且 acquired_flag=1）
     */
    UPPR_RATE("UPPR_RATE", "院内压力性损伤发生率", "例/千床日", 1000, null, 2, null, 3);

    private final String code;
    private final String label;
    /**
     * 单位：{@code %} 或例/千床日，直接落台账 unit 列
     */
    private final String unit;
    /**
     * 分子/分母的放大倍数：合格率 100，千床日率 1000
     */
    private final int multiplier;
    /**
     * 目标值字符串，null 表示不硬设目标
     */
    private final String target;
    /**
     * 事实来源：1-检查表 2-不良事件+住院事实
     */
    private final int sourceType;
    /**
     * 合格率类对应的检查类别，千床日类为 null
     */
    private final Integer checkCategory;
    /**
     * 千床日类对应的不良事件类型（不良事件上报的事件类型列），检查表类为 null
     */
    private final Integer adverseEventType;

    NursingIndicatorEnum(String code, String label, String unit, int multiplier, String target,
                         int sourceType, Integer checkCategory, Integer adverseEventType) {
        this.code = code;
        this.label = label;
        this.unit = unit;
        this.multiplier = multiplier;
        this.target = target;
        this.sourceType = sourceType;
        this.checkCategory = checkCategory;
        this.adverseEventType = adverseEventType;
    }

    public static NursingIndicatorEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (NursingIndicatorEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static String getText(String code) {
        NursingIndicatorEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (NursingIndicatorEnum e : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(e.code).append("-").append(e.label);
        }
        return sb.toString();
    }

    /**
     * 按本指标口径算指标值：分子 / 分母 × multiplier，保留两位。
     *
     * <p><b>分母为 0 返回 null 而不是 0</b>：「本月没有住院床日」和「跌倒发生率为 0」是两件事，
     * 写成 0 会让未重算的病区在对比图上冒充一个完美落点。
     */
    public BigDecimal rate(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.signum() <= 0) {
            return null;
        }
        BigDecimal num = numerator == null ? BigDecimal.ZERO : numerator;
        return num.multiply(BigDecimal.valueOf(multiplier)).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    /**
     * 目标值（{@code %} 类有，千床日类为 null=不硬设目标）
     */
    public BigDecimal targetDecimal() {
        return target == null ? null : new BigDecimal(target);
    }

    /**
     * 指标方向：true=越高越好（合格率），false=越低越好（发生率）
     */
    public boolean higherIsBetter() {
        return multiplier == 100;
    }
}
