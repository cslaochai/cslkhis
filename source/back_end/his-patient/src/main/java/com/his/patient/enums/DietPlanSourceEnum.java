package com.his.patient.enums;

import lombok.Getter;

/**
 * 膳食方案来源枚举
 */
@Getter
public enum DietPlanSourceEnum {

    ORDER_DERIVED(1, "医嘱校对派生"),
    NUTRITIONIST(2, "营养师手工登记");

    private final int code;
    private final String label;

    DietPlanSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DietPlanSourceEnum fromCode(int code) {
        for (DietPlanSourceEnum item : values()) {
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
        DietPlanSourceEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
