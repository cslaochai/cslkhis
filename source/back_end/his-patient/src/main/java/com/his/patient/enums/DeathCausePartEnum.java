package com.his.patient.enums;

import lombok.Getter;

/**
 * 死因链部分枚举
 */
@Getter
public enum DeathCausePartEnum {

    CHAIN(1, "Ⅰ部分死因链"),
    OTHER(2, "Ⅱ部分其他疾病");

    private final int code;
    private final String label;

    DeathCausePartEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeathCausePartEnum fromCode(int code) {
        for (DeathCausePartEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DeathCausePartEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
