package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 交叉配血方法枚举
 */
@Getter
public enum CrossmatchMethodEnum {

    SALINE(1, "盐水法"),
    POLYBRENE(2, "凝聚胺法"),
    COOMBS(3, "抗人球蛋白法"),
    MICRO_COLUMN_GEL(4, "微柱凝胶法");

    private final int code;
    private final String label;

    CrossmatchMethodEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CrossmatchMethodEnum fromCode(int code) {
        for (CrossmatchMethodEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        CrossmatchMethodEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
