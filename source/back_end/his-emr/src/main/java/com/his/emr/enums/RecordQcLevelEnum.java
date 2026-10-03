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

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        RecordQcLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
