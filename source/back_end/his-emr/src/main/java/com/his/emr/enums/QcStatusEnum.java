package com.his.emr.enums;

import lombok.Getter;

/**
 * 质控状态枚举
 */
@Getter
public enum QcStatusEnum {

    PENDING(1, "待处理"),
    PROCESSED(2, "已处理"),
    IGNORED(3, "已忽略");

    private final int code;
    private final String label;

    QcStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static QcStatusEnum fromCode(int code) {
        for (QcStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String labelOf(Integer code) {
        QcStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        QcStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
