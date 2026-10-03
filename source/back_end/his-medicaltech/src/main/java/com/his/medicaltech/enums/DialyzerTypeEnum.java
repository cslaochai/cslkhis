package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 透析器类型枚举
 */
@Getter
public enum DialyzerTypeEnum {

    LOW_FLUX_CELLULOSE(1, "低通量纤维素膜"),
    LOW_FLUX_SYNTHETIC(2, "低通量合成膜"),
    HIGH_FLUX_SYNTHETIC(3, "高通量合成膜");

    private final int code;
    private final String label;

    DialyzerTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DialyzerTypeEnum fromCode(int code) {
        for (DialyzerTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DialyzerTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
