package com.his.emr.enums;

import lombok.Getter;

/**
 * 病历终审定级枚举（1-甲级 2-乙级 3-丙级）
 */
@Getter
public enum QcGradeEnum {

    GRADE_A(1, "甲级"),
    GRADE_B(2, "乙级"),
    GRADE_C(3, "丙级");

    private final int code;
    private final String label;

    QcGradeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static QcGradeEnum fromCode(int code) {
        for (QcGradeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        QcGradeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        QcGradeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
