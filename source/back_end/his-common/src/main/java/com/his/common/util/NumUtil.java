package com.his.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 数值空值兜底、金额舍入与数量文本（全库唯一收口点）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
     *
     * <p><b>只有一个重载，不要再配一个 {@code (Integer, int)}</b>：本库分页参数是原始 {@code int}
     * （{@code PageParam.getPageNum()}），传入原始 int 时两个重载同时在「装箱阶段」可用，
     * 而 {@code int} 与 {@code Integer} 互不为子类型、没有更特者 → javac 报「对 orDefault 的引用不明确」，
     * 返回类型（int 还是 Integer）本来也不是调用方关心的差异。要原始 int 就地拆箱即可。
     */
    public static Integer orDefault(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }

    /**
     * null 当 0，再按 {@code scale} 位四舍五入（{@code HALF_UP}）。
     *
     * <p>小数位由调用方的技术阈值常量传入（精度属「多大」不是「某一列能取的值」，不枚举化），
     * 本方法只收口「先兜 null 再舍入」这一步：原先这 8 份私有副本里有三种写法
     * （{@code orZero().setScale()}、三元判空、null 时直接返回 {@code BigDecimal.ZERO}），
     * 最后一种返回的 0 带着 scale 0，与前两种的 {@code 0.00} 在文本输出上并不等价。
     */
    public static BigDecimal scale(BigDecimal value, int scale) {
        return orZero(value).setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 去掉无意义尾零后的十进制文本，null 进 null 出。
     *
     * <p><b>为什么必须是 {@code toPlainString} 而不是 {@code toString}</b>：{@code 1E+2} 的
     * {@code toString()} 带 {@code E}，同一个数量从除法结果来和从字面量来会得到两个不同字符串，
     * 而规范签名把数量的文本当成身份的一部分 —— 字符串一漂，历史签名全部校验不上。
     *
     * <p><b>null 出 null 是刻意的</b>：签名拼接要能区分「这个值没有」和「值为 0」。
     * 展示与报错文案要 {@code "0"} 的调用点写 {@code plain(orZero(v))}。
     */
    public static String plain(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }
}
