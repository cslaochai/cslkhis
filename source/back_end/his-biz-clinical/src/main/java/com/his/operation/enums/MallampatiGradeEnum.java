package com.his.operation.enums;

import lombok.Getter;

/**
 * Mallampati 气道分级
 */
@Getter
public enum MallampatiGradeEnum {

    CLASS_1(1, "Ⅰ"),
    CLASS_2(2, "Ⅱ"),
    CLASS_3(3, "Ⅲ"),
    CLASS_4(4, "Ⅳ");

    private final Integer code;
    private final String label;

    MallampatiGradeEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MallampatiGradeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MallampatiGradeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        MallampatiGradeEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        MallampatiGradeEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
