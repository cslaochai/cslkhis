package com.his.patient.enums;

import lombok.Getter;

/**
 * ICU 转出去向枚举
 */
@Getter
public enum IcuOutDestEnum {

    NORMAL_WARD(1, "普通病房"),
    SPECIAL_WARD(2, "专科病房"),
    OPERATION_ROOM(3, "手术室"),
    TRANSFER_HOSPITAL(4, "转院"),
    DEATH(5, "死亡"),
    SELF_DISCHARGE(6, "自动离院");

    private final int code;
    private final String label;

    IcuOutDestEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static IcuOutDestEnum fromCode(int code) {
        for (IcuOutDestEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        IcuOutDestEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        IcuOutDestEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
