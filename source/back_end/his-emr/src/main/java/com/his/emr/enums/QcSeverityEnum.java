package com.his.emr.enums;

/**
 * 质控问题严重度。
 *
 * <p><b>为什么严重度要决定扣分和结论两件事</b>：
 * 病案质控的结论是扣分制（甲级 ≥90 / 乙级 75~89 / 丙级 &lt;75），
 * 但同时又存在"单否项"——缺主诉、缺现病史、缺诊断、缺过敏史这类问题，
 * 无论其他项写得多好都直接判不合格。
 * 只用一个布尔值表达不了这个区别，所以严重度同时驱动
 * {@link #getDeduct()}（扣分）与引擎的否决判定。
 *
 * <p><b>口径不许自行调整</b>：扣分权重一旦改了，"甲级率"这个指标就不可比了。
 */
public enum QcSeverityEnum {

    /**
     * 无问题：不是"未知码值"，而是引擎认定的"这份病历没有问题"。
     * <p>
     * 它对应主单的 {@code severity_max = 0}（没有任何问题明细时的最大值），
     * 落在明细表质控问题明细.severity 上永远不会出现。
     * <p>
     * <b>为什么必须显式定义而不是靠"未知(n)"渲染</b>：满分病案占新质控单的四成，
     * 让它们统一显示成「未知(0)」既像 bug 又像数据缺失，会直接动摇整套质控的可信度；
     * 而如果改成 NULL，概览里 {@code severity_max < 3} 的甲级统计会因 SQL 三值逻辑静默漏算。
     */
    NONE(0, "无", 0),

    /**
     * 否决项：病历不成立。命中即判丙级、直接不通过
     */
    FATAL(3, "否决", 10),

    /**
     * 重要问题：影响诊疗安全或后续入组，不通过
     */
    MAJOR(2, "重要", 5),

    /**
     * 提示项：不规范但可接受，只提示不判不通过
     */
    MINOR(1, "提示", 2);

    private final int code;

    private final String text;

    private final int deduct;

    QcSeverityEnum(int code, String text, int deduct) {
        this.code = code;
        this.text = text;
        this.deduct = deduct;
    }

    public static String textOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (QcSeverityEnum severity : values()) {
            if (severity.code == code) {
                return severity.text;
            }
        }
        return "未知(" + code + ")";
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public int getDeduct() {
        return deduct;
    }
}
