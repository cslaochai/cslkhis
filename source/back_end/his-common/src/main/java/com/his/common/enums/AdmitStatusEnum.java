package com.his.common.enums;

import lombok.Getter;

/**
 * 在院状态枚举
 */
@Getter
public enum AdmitStatusEnum {

    DISCHARGED(0, "已出院"),
    IN_HOSPITAL(1, "在院");

    private final int code;
    private final String label;

    AdmitStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdmitStatusEnum fromCode(int code) {
        for (AdmitStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        AdmitStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
