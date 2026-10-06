package com.his.medicaltech.enums;

/**
 * 通知状态枚举
 */
public enum NotifyStatusEnum {

    NONE(0, "未通知"),
    SENT(1, "已通知");

    private final int code;
    private final String description;

    NotifyStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NotifyStatusEnum getByCode(int code) {
        for (NotifyStatusEnum status : NotifyStatusEnum.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return code != null && getByCode(code) != null;
    }

    /** 展示用：null 或不在枚举内返回空串（不把「未知」渲染给用户看） */
    public static String getText(Integer code) {
        NotifyStatusEnum item = code == null ? null : getByCode(code);
        return item == null ? "" : item.description;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        NotifyStatusEnum item = code == null ? null : getByCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.description;
    }
}
