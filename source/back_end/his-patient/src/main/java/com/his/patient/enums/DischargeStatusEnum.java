package com.his.patient.enums;

import lombok.Getter;

/**
 * 出院方式状态枚举
 */
@Getter
public enum DischargeStatusEnum {

    NORMAL(1, "正常"),
    TRANSFER(2, "转科"),
    AUTO(3, "自动出院");

    private final int code;
    private final String label;

    DischargeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DischargeStatusEnum fromCode(int code) {
        for (DischargeStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DischargeStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
