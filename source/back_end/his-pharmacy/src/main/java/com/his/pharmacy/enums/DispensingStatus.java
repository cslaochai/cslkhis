package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 发药状态枚举
 */
@Getter
public enum DispensingStatus {

    PENDING(1, "待发药"),
    DISPENSED(2, "已发药"),
    PICKED_UP(3, "已取药"),
    RETURNED(4, "已退药");

    private final int code;
    private final String label;

    DispensingStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DispensingStatus fromCode(int code) {
        for (DispensingStatus status : values()) {
            if (status.code == code) return status;
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /** 展示用：null 或脏值返回空串（不把「未知」渲染给用户看） */
    public static String getText(Integer code) {
        DispensingStatus item = code == null ? null : fromCode(code);
        return item == null ? "" : item.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」，保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        DispensingStatus item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
