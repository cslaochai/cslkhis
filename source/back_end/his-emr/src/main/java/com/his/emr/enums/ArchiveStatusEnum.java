package com.his.emr.enums;

import lombok.Getter;

/**
 * 病案归档状态枚举
 */
@Getter
public enum ArchiveStatusEnum {

    PENDING(1, "待归档"),
    ARCHIVED(2, "已归档"),
    SEALED(3, "已封存");

    private final int code;
    private final String label;

    ArchiveStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ArchiveStatusEnum fromCode(int code) {
        for (ArchiveStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        ArchiveStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
