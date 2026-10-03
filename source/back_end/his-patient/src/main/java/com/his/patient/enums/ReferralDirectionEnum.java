package com.his.patient.enums;

import lombok.Getter;

/**
 * 转诊方向枚举
 */
@Getter
public enum ReferralDirectionEnum {

    UP(1, "上转"),
    DOWN(2, "下转");

    private final int code;
    private final String label;

    ReferralDirectionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ReferralDirectionEnum fromCode(int code) {
        for (ReferralDirectionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        ReferralDirectionEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
