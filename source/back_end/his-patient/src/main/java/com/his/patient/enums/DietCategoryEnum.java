package com.his.patient.enums;

import lombok.Getter;

/**
 * 饮食类别枚举
 */
@Getter
public enum DietCategoryEnum {

    BASIC(1, "基本饮食"),
    THERAPY(2, "治疗饮食"),
    TEST(3, "诊断试验饮食"),
    SUPPORT(4, "营养支持");

    private final int code;
    private final String label;

    DietCategoryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DietCategoryEnum fromCode(int code) {
        for (DietCategoryEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DietCategoryEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
