package com.his.common.enums;

import lombok.Getter;

/**
 * 病理检查类型枚举（1-常规石蜡 2-术中冰冻 3-细胞学 4-免疫组化 5-疑难会诊）
 */
@Getter
public enum PathologyExamTypeEnum {

    PARAFFIN(1, "常规石蜡"),
    FROZEN(2, "术中冰冻"),
    CYTOLOGY(3, "细胞学"),
    IMMUNO(4, "免疫组化"),
    CONSULTATION(5, "疑难会诊");

    private final int code;
    private final String label;

    PathologyExamTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PathologyExamTypeEnum fromCode(int code) {
        for (PathologyExamTypeEnum type : values()) {
            if (type.code == code) return type;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        PathologyExamTypeEnum type = code == null ? null : fromCode(code);
        return type == null ? "" : type.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        PathologyExamTypeEnum type = code == null ? null : fromCode(code);
        if (type != null) {
            return type.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
