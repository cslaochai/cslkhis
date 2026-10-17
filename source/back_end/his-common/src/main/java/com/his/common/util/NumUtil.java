package com.his.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 数值空值兜底、金额舍入与数量文本（全库唯一收口点）。
 */
public final class NumUtil {

    /**
     * null 当 0。
     */
    public static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * null 当 0。
     */
    public static long orZero(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * null 当 0，用于金额与数量聚合。
     */
    public static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * null 取 {@code fallback}。
     */
    public static Integer orDefault(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }

    /**
     * null 当 0，再按 {@code scale} 位四舍五入（{@code HALF_UP}）。
     */
    public static BigDecimal scale(BigDecimal value, int scale) {
        return orZero(value).setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 去掉无意义尾零后的十进制文本，null 进 null 出。
     */
    public static String plain(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }
}
