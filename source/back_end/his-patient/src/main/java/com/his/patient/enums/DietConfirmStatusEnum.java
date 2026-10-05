package com.his.patient.enums;

import lombok.Getter;

/**
 * 膳食方案接收状态枚举
 */
@Getter
public enum DietConfirmStatusEnum {

    PENDING(0, "待接收"),
    DONE(1, "已接收"),
    REJECTED(2, "已退回");

    private final int code;
    private final String label;

    DietConfirmStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DietConfirmStatusEnum fromCode(int code) {
        for (DietConfirmStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        DietConfirmStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
