package com.his.common.enums;

import lombok.Getter;

/**
 * 病历质控类型枚举
 */
@Getter
public enum RecordQcTypeEnum {

    COMPREHENSIVE(0, "综合"),
    COMPLETENESS(1, "完整性检查"),
    NORMATIVE(2, "规范性检查"),
    LOGIC(3, "逻辑性检查"),
    AI_INHERENT(4, "AI内涵质控");

    private final int code;
    private final String label;

    RecordQcTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordQcTypeEnum fromCode(int code) {
        for (RecordQcTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        RecordQcTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        RecordQcTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
