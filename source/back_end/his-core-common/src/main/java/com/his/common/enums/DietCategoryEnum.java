package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 饮食类别枚举
 */
@Getter
public enum DietCategoryEnum {

    BASIC(1, "基本饮食"),
    THERAPY(2, "治疗饮食"),
    TEST(3, "诊断试验饮食"),
    SUPPORT(4, "营养支持");

    private final int code;
    private final String label;

    DietCategoryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DietCategoryEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DietCategoryEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DietCategoryEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DietCategoryEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DietCategoryEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
