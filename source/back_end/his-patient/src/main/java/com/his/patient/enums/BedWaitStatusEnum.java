package com.his.patient.enums;

import lombok.Getter;

/**
 * 床位等待状态枚举
 */
@Getter
public enum BedWaitStatusEnum {

    PENDING(0, "等待中"),
    ARRANGED(1, "已安排床位"),
    ADMITTED(2, "已收治"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String label;

    BedWaitStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedWaitStatusEnum fromCode(int code) {
        for (BedWaitStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        BedWaitStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
