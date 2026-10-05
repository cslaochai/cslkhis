package com.his.emr.enums;

import lombok.Getter;

/**
 * 医疗不良事件处理状态枚举
 */
@Getter
public enum AdverseEventStatusEnum {

    REPORTED(1, "已上报待处理"),
    HANDLED(2, "处理中"),
    RECTIFIED(3, "已整改"),
    CLOSED(4, "已结案");

    private final int code;
    private final String label;

    AdverseEventStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdverseEventStatusEnum fromCode(int code) {
        for (AdverseEventStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        AdverseEventStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
