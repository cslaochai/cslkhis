package com.his.emr.enums;

import lombok.Getter;

/**
 * 院感病例核实状态枚举
 */
@Getter
public enum InfectionCaseStatusEnum {

    PENDING(1, "待核实"),
    CONFIRMED(2, "已确认"),
    EXCLUDED(3, "已排除");

    private final int code;
    private final String label;

    InfectionCaseStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfectionCaseStatusEnum fromCode(int code) {
        for (InfectionCaseStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        InfectionCaseStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
