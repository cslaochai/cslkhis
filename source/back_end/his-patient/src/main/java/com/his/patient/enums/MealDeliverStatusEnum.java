package com.his.patient.enums;

import lombok.Getter;

/**
 * 配餐状态枚举
 */
@Getter
public enum MealDeliverStatusEnum {

    PENDING(0, "待配餐"),
    PREPARED(1, "已配餐"),
    DELIVERED(2, "已配送"),
    SIGNED(3, "已签收"),
    CANCELED(4, "已取消");

    private final int code;
    private final String label;

    MealDeliverStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MealDeliverStatusEnum fromCode(int code) {
        for (MealDeliverStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        MealDeliverStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
