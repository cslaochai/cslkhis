package com.his.report.support;

/**
 * 数据质量问题的严重度（P5.3）。
 *
 * <p>分级的唯一依据是"这条问题会不会影响临床判断或监管口径"：
 * <ul>
 *   <li>{@link #HIGH} —— 可能导致误诊、漏诊、费用/监管数据不可信（如性别与身份证矛盾、危急值超时未处置）</li>
 *   <li>{@link #MEDIUM} —— 影响数据可信度但有下游兜底（如缺住址、优惠未分摊到明细）</li>
 *   <li>{@link #LOW} —— 规范性问题，不影响当下业务（如病程重复书写）</li>
 * </ul>
 *
 * <p>注意：这里的 severity 是**数据质量的严重度**，与 AI 能力层的
 * {@code errorLevel}（只有硬规则能给 3，模型最大 2）是两套语义，不要混用。
 */
public enum QualitySeverity {

    HIGH(3, "严重"),
    MEDIUM(2, "警告"),
    LOW(1, "提示");

    private final int level;
    private final String text;

    QualitySeverity(int level, String text) {
        this.level = level;
        this.text = text;
    }

    public int getLevel() {
        return level;
    }

    public String getText() {
        return text;
    }

    public static String textOf(Integer level) {
        if (level == null) {
            return "—";
        }
        for (QualitySeverity s : values()) {
            if (s.level == level) {
                return s.text;
            }
        }
        return "";
    }
}
