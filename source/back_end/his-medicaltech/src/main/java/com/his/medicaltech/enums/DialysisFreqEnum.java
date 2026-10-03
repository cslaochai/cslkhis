package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 透析频次枚举
 */
@Getter
public enum DialysisFreqEnum {

    WEEK_1(1, "每周1次"),
    WEEK_2(2, "每周2次"),
    WEEK_3(3, "每周3次"),
    WEEK_4_PLUS(4, "每周≥4次");

    private final int code;
    private final String label;

    DialysisFreqEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DialysisFreqEnum fromCode(int code) {
        for (DialysisFreqEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DialysisFreqEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
