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

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」——
     * 脏数据应由数据治理流程修复，而非界面伪装。
     */
    public static String getText(Integer code) {
        DietCategoryEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        DietCategoryEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
