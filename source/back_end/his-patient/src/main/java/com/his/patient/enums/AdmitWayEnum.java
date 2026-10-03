package com.his.patient.enums;

import lombok.Getter;

/**
 * 入院途径枚举
 */
@Getter
public enum AdmitWayEnum {

    OUTPATIENT(1, "门诊"),
    EMERGENCY(2, "急诊"),
    TRANSFER(3, "转院"),
    OTHER(4, "其他");

    private final int code;
    private final String label;

    AdmitWayEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdmitWayEnum fromCode(int code) {
        for (AdmitWayEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        AdmitWayEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
