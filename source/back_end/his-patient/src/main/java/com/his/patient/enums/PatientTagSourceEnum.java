package com.his.patient.enums;

import lombok.Getter;

/**
 * 患者标签来源枚举
 */
@Getter
public enum PatientTagSourceEnum {

    MANUAL(1, "手动打标"),
    AUTO(2, "系统自动打标");

    private final int code;
    private final String label;

    PatientTagSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PatientTagSourceEnum fromCode(int code) {
        for (PatientTagSourceEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        PatientTagSourceEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
