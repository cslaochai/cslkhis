package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 纠纷投诉来源枚举
 */
@Getter
public enum DisputeSourceEnum {

    CALL(1, "来电"),
    VISIT(2, "来访"),
    LETTER(3, "来信"),
    GOV_HOTLINE(4, "政务热线"),
    SUPERIOR(5, "上级交办"),
    INTERNAL(6, "院内发现"),
    OTHER(7, "其他");

    private final int code;
    private final String label;

    DisputeSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DisputeSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DisputeSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DisputeSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DisputeSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DisputeSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
