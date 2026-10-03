package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 血液来源枚举
 */
@Getter
public enum BloodSourceTypeEnum {

    STATION(1, "血站"),
    AUTO_DONATION(2, "自体储血"),
    MUTUAL_AID(3, "互助献血");

    private final int code;
    private final String label;

    BloodSourceTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BloodSourceTypeEnum fromCode(int code) {
        for (BloodSourceTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        BloodSourceTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
