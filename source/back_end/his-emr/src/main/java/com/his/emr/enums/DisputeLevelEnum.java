package com.his.emr.enums;

import lombok.Getter;

/**
 * 纠纷投诉等级枚举
 */
@Getter
public enum DisputeLevelEnum {

    ORDINARY(1, "一般"),
    LARGER(2, "较大"),
    MAJOR(3, "重大");

    private final int code;
    private final String label;

    DisputeLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DisputeLevelEnum fromCode(int code) {
        for (DisputeLevelEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        DisputeLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        DisputeLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
