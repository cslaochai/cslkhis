package com.his.patient.enums;

import lombok.Getter;

/**
 * 收治指令状态枚举
 */
@Getter
public enum AdmissionOrderStatusEnum {

    PENDING(1, "待收治"),
    ADMITTED(2, "已收治"),
    VOIDED(3, "已作废"),
    EXPIRED(4, "已过期");

    private final int code;
    private final String label;

    AdmissionOrderStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdmissionOrderStatusEnum fromCode(int code) {
        for (AdmissionOrderStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        AdmissionOrderStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
