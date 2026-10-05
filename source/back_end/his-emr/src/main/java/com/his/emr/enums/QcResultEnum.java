package com.his.emr.enums;

import lombok.Getter;

/**
 * 质控结果枚举（0-不通过 1-通过）
 */
@Getter
public enum QcResultEnum {

    NOT_PASS(0, "不通过"),
    PASS(1, "通过");

    private final int code;
    private final String label;

    QcResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static QcResultEnum fromCode(int code) {
        for (QcResultEnum item : values()) {
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
        QcResultEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        QcResultEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
