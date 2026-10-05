package com.his.patient.enums;

import lombok.Getter;

/**
 * 离院方式枚举
 */
@Getter
public enum DischargeWayEnum {

    MEDICAL_ORDER(1, "医嘱离院"),
    TRANSFER_HOSPITAL(2, "医嘱转院"),
    TRANSFER_COMMUNITY(3, "医嘱转社区"),
    UNMEDICAL(4, "非医嘱离院"),
    DEATH(5, "死亡"),
    OTHER(9, "其他");

    private final int code;
    private final String label;

    DischargeWayEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DischargeWayEnum fromCode(int code) {
        for (DischargeWayEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        DischargeWayEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
