package com.his.patient.enums;

import lombok.Getter;

/**
 * 营养筛查时机枚举
 */
@Getter
public enum NutritionScreenSourceEnum {

    ADMIT(1, "入院48小时内"),
    CHANGE(2, "病情变化复筛"),
    POST_OP(3, "术后复筛"),
    PERIODIC(4, "定期复筛");

    private final int code;
    private final String label;

    NutritionScreenSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NutritionScreenSourceEnum fromCode(int code) {
        for (NutritionScreenSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        NutritionScreenSourceEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
