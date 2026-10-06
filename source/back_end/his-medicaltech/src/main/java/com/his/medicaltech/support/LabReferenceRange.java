package com.his.medicaltech.support;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 解析后的检验参考区间（值对象）。
 * <p>
 * <b>为什么要专门解析，而不是直接字符串比较：</b>
 * 库里的 {@code reference_range} 是人工维护的自由文本，实测存在至少 6 种写法：
 * <pre>
 *   4-10                     双侧区间
 *   男120-160/女110-150      按性别分支（斜杠分隔）
 *   男:0-15, 女:0-20         按性别分支（冒号 + 逗号分隔）
 *   &lt;5.2 / &gt;30             单侧
 *   阴性 / 阴性/阳性定性结果
 *   0-5/HP                  带单位后缀
 *   根据实验室标准根本无法判定
 * </pre>
 * 直接 {@code equals} 或 {@code contains} 只会得到「永远不异常」或「永远异常」两种结果，
 * 而这两种错误都不会报错 —— 参见 {@link LabAbnormalJudge} 的类注释。
 * <p>
 * {@link Kind#UNPARSABLE} 是<b>刻意保留</b>的一等公民：解析不出来时必须如实说
 * 「不知道」，由调用方决定保守行为，而不是猜一个区间出来。
 */
public record LabReferenceRange(Kind kind,
                                Double lower,
                                Double upper,
                                boolean lowerInclusive,
                                boolean upperInclusive,
                                Set<String> acceptableQualitative,
                                String sourceText) {

    private static final Set<String> EMPTY = Set.of();

    public static LabReferenceRange unparsable(String sourceText) {
        return new LabReferenceRange(Kind.UNPARSABLE, null, null, false, false, EMPTY, sourceText);
    }

    public static LabReferenceRange range(double lower, double upper,
                                          boolean lowerInclusive, boolean upperInclusive,
                                          String sourceText) {
        return new LabReferenceRange(Kind.RANGE, lower, upper, lowerInclusive, upperInclusive, EMPTY, sourceText);
    }

    public static LabReferenceRange upperOnly(double upper, boolean inclusive, String sourceText) {
        return new LabReferenceRange(Kind.UPPER_ONLY, null, upper, false, inclusive, EMPTY, sourceText);
    }

    public static LabReferenceRange lowerOnly(double lower, boolean inclusive, String sourceText) {
        return new LabReferenceRange(Kind.LOWER_ONLY, lower, null, inclusive, false, EMPTY, sourceText);
    }

    public static LabReferenceRange qualitative(Set<String> acceptable, String sourceText) {
        return new LabReferenceRange(Kind.QUALITATIVE, null, null, false, false,
                new LinkedHashSet<>(acceptable), sourceText);
    }

    private static String format(Double value) {
        if (value == null) {
            return "";
        }
        // 去掉 4.0 这种多余的小数末尾 0，让描述与医生习惯一致
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf(value.longValue());
        }
        return String.valueOf(value);
    }

    /**
     * 是否可用于自动判定。{@link Kind#UNPARSABLE} 返回 false。
     */
    public boolean usable() {
        return kind != Kind.UNPARSABLE;
    }

    public boolean isQualitative() {
        return kind == Kind.QUALITATIVE;
    }

    /**
     * 生成给人看的区间描述，用于 fill abnormalDesc / 提示词。
     * 注意：无法解析时返回原文而不是空串 —— 让人看到「根据实验室标准」
     * 才能理解为什么没判定，返回空串只会让人以为字段丢了。
     */
    public String describe() {
        return switch (kind) {
            case RANGE -> format(lower) + "-" + format(upper);
            case UPPER_ONLY -> (upperInclusive ? "≤" : "<") + format(upper);
            case LOWER_ONLY -> (lowerInclusive ? "≥" : ">") + format(lower);
            case QUALITATIVE -> String.join("/", acceptableQualitative);
            case UNPARSABLE -> sourceText == null ? "" : sourceText;
        };
    }

    public enum Kind {
        /**
         * 双侧闭/开区间
         */
        RANGE,
        /**
         * 仅有上限（{@code <5.2}）
         */
        UPPER_ONLY,
        /**
         * 仅有下限（{@code >30}）
         */
        LOWER_ONLY,
        /**
         * 定性结果（阴性/阳性/相合…）
         */
        QUALITATIVE,
        /**
         * 无法解析 —— 调用方<b>不得</b>据此判定异常
         */
        UNPARSABLE
    }
}
