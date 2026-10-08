package com.his.common.util;

import com.his.common.exception.BusinessException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * 字符串清洗与截断（全库唯一收口点）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextUtil {

    /**
     * 判「有没有内容」：null 或全空白为 {@code false}。只管给布尔，不改写值 —— 要洗值用 {@link #trimToNull(String)}。
     *
     * <p>反向一律写 {@code !hasText(x)}，**不提供 {@code isBlank} 第二个谓词**：
     * 收口前全库有 7 份私有副本（{@code isText} 3 份、{@code isBlank} 3 份、{@code notBlank} 1 份），
     * 正向与取反两种形状各写各的，改判空口径时必须同时找齐两份，漏一份就是一个脏数据入口。
     *
     * <p>形参用 {@code CharSequence} 而非 {@code String}：判空与值的类型无关，而调用侧有的是
     * {@code StringBuilder}/{@code Stream<CharSequence>}（收窄成 String 会让这些点编译不过）。
     */
    public static boolean hasText(CharSequence value) {
        return StringUtils.hasText(value);
    }

    /**
     * 去首尾空白；null 进 null 出（不把 null 变成空串）。
     */
    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 去首尾空白，全空白折叠成 null —— 用作查询条件与「填了才算有值」的写入。
     */
    public static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /**
     * 去首尾空白，null 折叠成空串 —— 用于拼接与展示，避免 {@code "null"} 字面量。
     */
    public static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * null 折叠成空串，原样保留空白（只防 NPE，不做清洗）。
     */
    public static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * 空白（null 或全空格）取 fallback，否则取去空白后的原值。
     */
    public static String blankToDefault(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    /**
     * 去首尾空白后截到 {@code max} 个字符；null 进 null 出。
     *
     * <p>写长文本（原因/描述/备注）前一律先过这里：超长写进去报「数据过长」，
     * 会把一次正常的保存变成服务端异常，用户连失败原因都看不到。
     */
    public static String cut(String value, int max) {
        String s = value == null ? null : value.trim();
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }

    /**
     * 同 {@link #cut(String, int)}，但空白（含 null）折叠成 {@code blankValue}。
     */
    public static String cut(String value, int max, String blankValue) {
        return StringUtils.hasText(value) ? cut(value, max) : blankValue;
    }

    /**
     * 同 {@link #cut(String, int)}，但空白折叠成 null（与 {@link #trimToNull(String)} 同口径的截断版）。
     */
    public static String cutToNull(String value, int max) {
        return StringUtils.hasText(value) ? cut(value, max) : null;
    }

    /**
     * 超长时截断并以省略号结尾，返回值总长度不超过 {@code max}；空白返回空串。
     *
     * <p>只用于展示与提示文案，写库请走 {@link #cut(String, int)}。
     */
    public static String ellipsis(String value, int max) {
        String s = trimToEmpty(value);
        return s.length() <= max ? s : s.substring(0, Math.max(0, max - 1)) + "…";
    }

    /**
     * 条件必填的文本：空白（null 或全空格）抛业务异常，否则返回去首尾空白后的值。
     *
     * <p><b>为什么留在工具类而不是 DTO 注解</b>：{@code @NotBlank} 是一刀切，管不了
     * 「同一接口内按请求内容分支」的必填 —— 作废原因只在取消/拒绝入口必填，确认入口根本不传这个字段；
     * 加了注解就把合法的确认请求挡成 400（AGENTS.md §10 条件必填那一类）。
     *
     * <p><b>为什么必须是唯一一份</b>：收口前全库 5 份私有 {@code requireText}，两种形状
     * （抛异常前 trim 与不 trim、文案自带整句与只给字段名再拼「不能为空」），
     * 同一个动作在不同页面弹出的提示不一样。文案由调用方整句给出，本方法不猜字段名。
     */
    public static String requireTrimmed(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BusinessException(message);
        }
        return trimmed;
    }
}
