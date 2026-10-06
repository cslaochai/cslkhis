package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 配血结论枚举
 */
@Getter
public enum CrossmatchResultEnum {

    MATCHED(1, "相合"),
    UNMATCHED(2, "不相合"),
    SUSPECT_AGGLUTINATION(3, "可疑凝集");

    private final int code;
    private final String label;

    CrossmatchResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CrossmatchResultEnum fromCode(int code) {
        for (CrossmatchResultEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /** 展示用：null 或不在枚举内返回空串（不把「未知」渲染给用户看） */
    public static String getText(Integer code) {
        CrossmatchResultEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        CrossmatchResultEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
