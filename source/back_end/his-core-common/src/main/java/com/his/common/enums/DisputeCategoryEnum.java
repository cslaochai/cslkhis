package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    public static DisputeCategoryEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DisputeCategoryEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DisputeCategoryEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DisputeCategoryEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DisputeCategoryEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
