package com.his.emr.enums;

import lombok.Getter;

/**
 * 院感感染来源枚举
 */
@Getter
public enum InfectionSourceEnum {

    COMMUNITY(1, "社区感染"),
    HOSPITAL(2, "医院感染");

    private final int code;
    private final String label;

    InfectionSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfectionSourceEnum fromCode(int code) {
        for (InfectionSourceEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        InfectionSourceEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
