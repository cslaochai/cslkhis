package com.his.patient.enums;

import lombok.Getter;

/**
 * 床位状态枚举
 */
@Getter
public enum BedStatusEnum {

    REPAIR(0, "维修"),
    FREE(1, "空闲"),
    OCCUPIED(2, "占用"),
    LOCKED(3, "锁定");

    private final int code;
    private final String label;

    BedStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedStatusEnum fromCode(int code) {
        for (BedStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        BedStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
