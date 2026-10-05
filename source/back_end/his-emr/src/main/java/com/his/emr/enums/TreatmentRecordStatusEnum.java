package com.his.emr.enums;

import lombok.Getter;

/**
 * 治疗记录状态枚举
 */
@Getter
public enum TreatmentRecordStatusEnum {

    ABNORMAL(0, "异常"),
    NORMAL(1, "正常");

    private final int code;
    private final String label;

    TreatmentRecordStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TreatmentRecordStatusEnum fromCode(int code) {
        for (TreatmentRecordStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        TreatmentRecordStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
