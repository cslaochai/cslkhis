package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 透析机位状态枚举
 */
@Getter
public enum DialysisMachineStatusEnum {

    USABLE(1, "可用"),
    REPAIR(2, "维修"),
    OFF(3, "停用");

    private final int code;
    private final String label;

    DialysisMachineStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DialysisMachineStatusEnum fromCode(int code) {
        for (DialysisMachineStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DialysisMachineStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
