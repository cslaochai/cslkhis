package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 答卷填报方式枚举
 */
@Getter
public enum FillSourceEnum {

    SELF(1, "患者自填"),
    AGENT(2, "随访员代填"),
    QR(3, "现场扫码");

    private final int code;
    private final String label;

    FillSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FillSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (FillSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        FillSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        FillSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        FillSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
