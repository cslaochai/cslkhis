package com.his.emr.enums;

import lombok.Getter;

/**
 * 病历终审判定等级
 */
@Getter
public enum RecordQcGradeEnum {

    GRADE_A(1, "甲级"),
    GRADE_B(2, "乙级"),
    GRADE_C(3, "丙级");

    private final Integer code;
    private final String label;

    RecordQcGradeEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordQcGradeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RecordQcGradeEnum item : values()) {
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
        RecordQcGradeEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        RecordQcGradeEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
