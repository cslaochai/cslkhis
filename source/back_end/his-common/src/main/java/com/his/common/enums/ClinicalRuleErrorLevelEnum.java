package com.his.common.enums;

import lombok.Getter;

/**
 * 临床规则校验的错误级别枚举（1-提示 2-警告 3-严重）。
 * <p>
 * 3-严重 仅由确定性硬规则给出：模型返回该级别时一律降级，拦截权不交给概率系统。
 */
@Getter
public enum ClinicalRuleErrorLevelEnum {

    HINT(1, "提示"),
    WARN(2, "警告"),
    BLOCK(3, "严重");

    private final int code;
    private final String label;

    ClinicalRuleErrorLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ClinicalRuleErrorLevelEnum fromCode(int code) {
        for (ClinicalRuleErrorLevelEnum item : values()) {
            if (item.code == code) return item;
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
        ClinicalRuleErrorLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        ClinicalRuleErrorLevelEnum item = code == null ? null : fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
