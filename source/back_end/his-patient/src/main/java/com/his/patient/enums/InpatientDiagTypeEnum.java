package com.his.patient.enums;

import lombok.Getter;

/**
 * 住院诊断类型枚举
 */
@Getter
public enum InpatientDiagTypeEnum {

    MAIN(1, "主要诊断"),
    OTHER(2, "其他诊断");

    private final int code;
    private final String label;

    InpatientDiagTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InpatientDiagTypeEnum fromCode(int code) {
        for (InpatientDiagTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        InpatientDiagTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
