package com.his.common.util;

import com.his.common.exception.BusinessException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * 字符串清洗与截断（全库唯一收口点）。
 */
public final class TextUtil {

    /**
     * 判「有没有内容」：null 或全空白为 {@code false}。只管给布尔，不改写值 —— 要洗值用 {@link #trimToNull(String)}。
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
     */
    public static String ellipsis(String value, int max) {
        String s = trimToEmpty(value);
        return s.length() <= max ? s : s.substring(0, Math.max(0, max - 1)) + "…";
    }

    /**
     * 条件必填的文本：空白（null 或全空格）抛业务异常，否则返回去首尾空白后的值。
     */
    public static String requireTrimmed(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BusinessException(message);
        }
        return trimmed;
    }
}
