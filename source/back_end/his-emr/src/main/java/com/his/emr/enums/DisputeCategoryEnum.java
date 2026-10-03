package com.his.emr.enums;

import lombok.Getter;

/**
 * 纠纷投诉类别枚举
 */
@Getter
public enum DisputeCategoryEnum {

    SERVICE_COMPLAINT(1, "服务投诉"),
    MEDICAL_DISPUTE(2, "医疗纠纷"),
    INJURY_DISPUTE(3, "医疗损害争议"),
    OTHER(4, "其他");

    private final int code;
    private final String label;

    DisputeCategoryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DisputeCategoryEnum fromCode(int code) {
        for (DisputeCategoryEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DisputeCategoryEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
