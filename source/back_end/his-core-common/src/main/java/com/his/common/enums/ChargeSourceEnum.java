package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 手术麻醉收费来源（1-麻醉记录 2-PACU复苏 3-手术（预留））。
 */
@Getter
public enum ChargeSourceEnum {

    RECORD(1, "麻醉记录"),
    PACU(2, "PACU复苏"),
    OPERATION(3, "手术");

    private final int code;
    private final String label;

    ChargeSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static ChargeSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ChargeSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        ChargeSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ChargeSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ChargeSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
