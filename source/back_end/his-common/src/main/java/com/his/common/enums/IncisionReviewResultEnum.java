package com.his.common.enums;

import lombok.Getter;

/**
 * I 类切口预防用药点评结论枚举（合理 / 不合理）。
 */
@Getter
public enum IncisionReviewResultEnum {

    REASONABLE(1, "合理"),
    UNREASONABLE(2, "不合理");

    private final int code;
    private final String label;

    IncisionReviewResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static IncisionReviewResultEnum fromCode(int code) {
        for (IncisionReviewResultEnum item : values()) {
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
        IncisionReviewResultEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        IncisionReviewResultEnum item = code == null ? null : fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
