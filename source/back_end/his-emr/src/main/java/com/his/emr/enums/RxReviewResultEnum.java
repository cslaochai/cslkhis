package com.his.emr.enums;

import lombok.Getter;

/**
 * 处方点评结论枚举
 */
@Getter
public enum RxReviewResultEnum {

    REASONABLE(1, "合理"),
    IRREGULAR(2, "不规范处方"),
    UNSUITABLE(3, "用药不适宜处方"),
    ABNORMAL(4, "超常处方");

    private final int code;
    private final String label;

    RxReviewResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RxReviewResultEnum fromCode(int code) {
        for (RxReviewResultEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。
     */
    public static String labelOf(Integer code) {
        RxReviewResultEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
