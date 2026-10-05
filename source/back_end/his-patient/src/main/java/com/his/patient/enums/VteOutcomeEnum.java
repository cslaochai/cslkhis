package com.his.patient.enums;

import lombok.Getter;

/**
 * VTE 结局枚举（4=未知 是合法业务值，不是兜底）
 */
@Getter
public enum VteOutcomeEnum {

    IMPROVED(1, "好转"),
    NOT_CURED(2, "未愈"),
    DEATH(3, "死亡"),
    UNKNOWN(4, "未知");

    private final int code;
    private final String label;

    VteOutcomeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VteOutcomeEnum fromCode(int code) {
        for (VteOutcomeEnum item : values()) {
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
        VteOutcomeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        VteOutcomeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
