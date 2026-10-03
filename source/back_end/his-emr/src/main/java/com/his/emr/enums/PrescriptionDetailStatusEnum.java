package com.his.emr.enums;

import lombok.Getter;

/**
 * 处方明细状态枚举
 */
@Getter
public enum PrescriptionDetailStatusEnum {

    NORMAL(1, "正常"),
    DISPENSED(2, "已发药"),
    RETURNED(3, "已退药");

    private final int code;
    private final String label;

    PrescriptionDetailStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PrescriptionDetailStatusEnum fromCode(int code) {
        for (PrescriptionDetailStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        PrescriptionDetailStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
