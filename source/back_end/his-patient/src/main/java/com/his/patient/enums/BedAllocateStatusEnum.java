package com.his.patient.enums;

import lombok.Getter;

/**
 * 床位分配状态枚举
 */
@Getter
public enum BedAllocateStatusEnum {

    RESERVED(1, "已预留"),
    ADMITTED(2, "已转入院"),
    RELEASED(3, "已释放"),
    VOID(4, "已作废");

    private final int code;
    private final String label;

    BedAllocateStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedAllocateStatusEnum fromCode(int code) {
        for (BedAllocateStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        BedAllocateStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
