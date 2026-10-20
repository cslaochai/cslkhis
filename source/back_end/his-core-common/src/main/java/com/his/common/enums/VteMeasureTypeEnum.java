package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * VTE 预防措施类别枚举
 */
@Getter
public enum VteMeasureTypeEnum {

    BASIC(1, "基础预防"),
    PHYSICAL(2, "物理预防"),
    DRUG(3, "药物预防");

    private final int code;
    private final String label;

    VteMeasureTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VteMeasureTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (VteMeasureTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        VteMeasureTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        VteMeasureTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        VteMeasureTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
