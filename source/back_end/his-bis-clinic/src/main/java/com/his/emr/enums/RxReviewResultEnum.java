package com.his.emr.enums;

import lombok.Getter;

/**
 * 处方点评结论枚举
 */
@Getter
public enum RxReviewResultEnum {

    REASONABLE(1, "合理"),
    IRREGULAR(2, "不规范处方"),
    UNSUITABLE(3, "用药不适宜处方"),
    ABNORMAL(4, "超常处方");

    private final int code;
    private final String label;

    RxReviewResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RxReviewResultEnum fromCode(int code) {
        for (RxReviewResultEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        RxReviewResultEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        RxReviewResultEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
