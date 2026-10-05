package com.his.emr.enums;

import lombok.Getter;

/**
 * 慢病档案认定状态枚举
 */
@Getter
public enum ChronicConfirmStatusEnum {

    CANCELLED(2, "已取消"),
    CONFIRMED(1, "已认定"),
    PENDING(0, "待认定");

    private final int code;
    private final String label;

    ChronicConfirmStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ChronicConfirmStatusEnum fromCode(int code) {
        for (ChronicConfirmStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。
     */
    public static String labelOf(Integer code) {
        ChronicConfirmStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
