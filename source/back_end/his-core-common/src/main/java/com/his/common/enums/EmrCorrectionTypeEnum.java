package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病历更正申请类型
 */
@Getter
public enum EmrCorrectionTypeEnum {

    SELF(1, "经治医生更正"),
    SUPERIOR(2, "上级代改");

    private final int code;
    private final String label;

    EmrCorrectionTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EmrCorrectionTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EmrCorrectionTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        EmrCorrectionTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        EmrCorrectionTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        EmrCorrectionTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
