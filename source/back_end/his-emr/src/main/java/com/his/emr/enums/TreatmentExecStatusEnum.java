package com.his.emr.enums;

import lombok.Getter;

/**
 * 治疗执行状态枚举
 */
@Getter
public enum TreatmentExecStatusEnum {

    PENDING(0, "待执行"),
    DONE(1, "已执行"),
    CANCELLED(2, "已取消");

    private final int code;
    private final String label;

    TreatmentExecStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TreatmentExecStatusEnum fromCode(int code) {
        for (TreatmentExecStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        TreatmentExecStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
