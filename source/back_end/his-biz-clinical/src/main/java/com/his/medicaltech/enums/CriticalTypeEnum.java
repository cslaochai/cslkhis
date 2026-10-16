package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 危急值类型枚举
 */
@Getter
public enum CriticalTypeEnum {

    LOW(1, "偏低"),
    HIGH(2, "偏高");

    private final int code;
    private final String label;

    CriticalTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CriticalTypeEnum fromCode(int code) {
        for (CriticalTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用：null 或不在枚举内返回空串（不把「未知」渲染给用户看）
     */
    public static String getText(Integer code) {
        CriticalTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        CriticalTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
