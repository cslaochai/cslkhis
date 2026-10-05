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
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」——
     * 脏数据应由数据治理流程修复，而非界面伪装。
     */
    public static String getText(Integer code) {
        MealDeliverStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），
     * 保留原始码值便于排查脏数据。绝不用它喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        MealDeliverStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
