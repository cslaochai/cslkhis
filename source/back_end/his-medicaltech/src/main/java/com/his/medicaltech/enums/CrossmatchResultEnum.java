package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 配血结论枚举
 */
@Getter
public enum CrossmatchResultEnum {

    MATCHED(1, "相合"),
    UNMATCHED(2, "不相合"),
    SUSPECT_AGGLUTINATION(3, "可疑凝集");

    private final int code;
    private final String label;

    CrossmatchResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CrossmatchResultEnum fromCode(int code) {
        for (CrossmatchResultEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        CrossmatchResultEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
