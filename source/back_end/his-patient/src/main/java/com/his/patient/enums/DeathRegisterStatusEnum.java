package com.his.patient.enums;

import lombok.Getter;

/**
 * 死亡登记状态枚举
 */
@Getter
public enum DeathRegisterStatusEnum {

    DRAFT(1, "草稿"),
    DONE(2, "已登记"),
    VOIDED(3, "已作废");

    private final int code;
    private final String label;

    DeathRegisterStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeathRegisterStatusEnum fromCode(int code) {
        for (DeathRegisterStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DeathRegisterStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
