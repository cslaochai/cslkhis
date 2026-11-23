package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 取药签到方式枚举
 */
@Getter
public enum DispenseSignInTypeEnum {

    ID_CARD(1, "身份证"),
    PATIENT_CARD(2, "就诊卡"),
    SCAN_CODE(3, "扫码"),
    MANUAL(4, "手工");

    private final int code;
    private final String label;

    DispenseSignInTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DispenseSignInTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DispenseSignInTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DispenseSignInTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DispenseSignInTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DispenseSignInTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
