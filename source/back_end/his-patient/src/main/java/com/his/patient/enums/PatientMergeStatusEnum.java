package com.his.patient.enums;

import lombok.Getter;

/**
 * 患者主索引状态枚举
 */
@Getter
public enum PatientMergeStatusEnum {

    NORMAL(0, "正常"),
    MERGED(1, "已并入主档");

    private final int code;
    private final String label;

    PatientMergeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PatientMergeStatusEnum fromCode(int code) {
        for (PatientMergeStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        PatientMergeStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
