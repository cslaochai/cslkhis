package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 死因链部分枚举
 */
@Getter
public enum DeathCausePartEnum {

    CHAIN(1, "Ⅰ部分死因链"),
    OTHER(2, "Ⅱ部分其他疾病");

    private final int code;
    private final String label;

    DeathCausePartEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeathCausePartEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DeathCausePartEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DeathCausePartEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DeathCausePartEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DeathCausePartEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
