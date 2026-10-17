package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 会诊类别枚举
 */
@Getter
public enum ConsultCategoryEnum {

    NORMAL(1, "普通科间会诊"),
    NUTRITION(2, "营养会诊"),
    PHARMACY(3, "药学会诊"),
    OTHER(4, "其他专科会诊");

    private final int code;
    private final String label;

    ConsultCategoryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static ConsultCategoryEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ConsultCategoryEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ConsultCategoryEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ConsultCategoryEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ConsultCategoryEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
