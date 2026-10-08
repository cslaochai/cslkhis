package com.his.medicaltech.support;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 解析后的检验参考区间（值对象）。
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
