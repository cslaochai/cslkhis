package com.his.patient.enums;

import lombok.Getter;

/**
 * 住院请假类别枚举
 */
@Getter
public enum InpatientLeaveTypeEnum {

    DAY_TRIP(1, "临时外出当日往返"),
    OVERNIGHT(2, "离院过夜"),
    OTHER(9, "其他");

    private final int code;
    private final String label;

    InpatientLeaveTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InpatientLeaveTypeEnum fromCode(int code) {
        for (InpatientLeaveTypeEnum item : values()) {
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
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。
     */
    public static String getText(Integer code) {
        InpatientLeaveTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        InpatientLeaveTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
