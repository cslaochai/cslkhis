package com.his.emr.enums;

import lombok.Getter;

/**
 * 病历质控动作发生级枚举
 */
@Getter
public enum RecordQcLevelEnum {

    DEPT(1, "科级"),
    ARCHIVE(2, "病案室"),
    MEDAFFAIRS(3, "医务处");

    private final int code;
    private final String label;

    RecordQcLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordQcLevelEnum fromCode(int code) {
        for (RecordQcLevelEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        RecordQcLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        RecordQcLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
