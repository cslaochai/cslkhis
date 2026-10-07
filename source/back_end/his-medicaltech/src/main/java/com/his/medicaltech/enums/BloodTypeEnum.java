package com.his.medicaltech.enums;

import com.his.common.util.TextUtil;
import lombok.Getter;

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

    public static BloodTypeEnum fromCode(int code) {
        for (BloodTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        BloodTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」）。
     */
    public static String labelOrUnknown(Integer code) {
        BloodTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
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
}
