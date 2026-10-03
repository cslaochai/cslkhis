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

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。 */
    public static String labelOf(Integer code) {
        RecordQcTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
