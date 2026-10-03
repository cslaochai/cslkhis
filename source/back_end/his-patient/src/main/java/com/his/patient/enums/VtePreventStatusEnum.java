package com.his.patient.enums;

import lombok.Getter;

/**
 * VTE 预防措施落实状态枚举
 */
@Getter
public enum VtePreventStatusEnum {

    PENDING(0, "待落实"),
    DONE(1, "已落实"),
    CONTRAINDICATION(2, "禁忌未用"),
    REFUSED(3, "患者拒绝");

    private final int code;
    private final String label;

    VtePreventStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VtePreventStatusEnum fromCode(int code) {
        for (VtePreventStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        VtePreventStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
