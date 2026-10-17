package com.his.common.enums;

import com.his.common.util.TextUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * ABO 血型枚举
 */
@Getter
public enum BloodTypeEnum {

    A(1, "A"),
    B(2, "B"),
    O(3, "O"),
    AB(4, "AB");

    private final int code;
    private final String label;

    BloodTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    // ── 输血链的 ABO 血型是**字符串码**（A / B / O / AB），与上面的字典码不是同一列 ──

    /**
     * 字符串码取枚举项：大小写与前后空格一律忽略（"a " 与 "A" 是同一血型）。
     */
    public static BloodTypeEnum fromAbo(String abo) {
        if (!TextUtil.hasText(abo)) {
            return null;
        }
        String trimmed = abo.trim().toUpperCase();
        for (BloodTypeEnum item : values()) {
            if (item.name().equals(trimmed)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 字符串码是否合法（写入侧校验用；null 与空串都不合法）
     */
    public static boolean isValidAbo(String abo) {
        return fromAbo(abo) != null;
    }

    /**
     * 规整 ABO 写法（null 原样返回，其余 trim + 转大写）
     */
    public static String normalizeAbo(String abo) {
        return abo == null ? null : abo.trim().toUpperCase();
    }

    /**
     * 字符串码展示文案：null / 空 / 词表外返回空串 —— 血型猜错比没有文案更危险。
     */
    public static String getAboText(String abo) {
        BloodTypeEnum item = fromAbo(abo);
        return item == null ? "" : item.label;
    }

    public static BloodTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BloodTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        BloodTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BloodTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BloodTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
