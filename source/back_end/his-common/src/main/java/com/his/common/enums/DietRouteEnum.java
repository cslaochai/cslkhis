package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 给食途径枚举
 */
@Getter
public enum DietRouteEnum {

    ORAL(1, "口服"),
    TUBE(2, "管饲"),
    IV(3, "静脉（肠外）");

    private final int code;
    private final String label;

    DietRouteEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DietRouteEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DietRouteEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DietRouteEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DietRouteEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DietRouteEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
