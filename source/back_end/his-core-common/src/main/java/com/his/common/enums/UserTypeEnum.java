package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 登录账号类型
 */
@Getter
public enum UserTypeEnum {

    INNER(1, "院内用户"),
    OTHER_DOCTOR(2, "院外用户"),
    PATIENT(3, "患者"),
    OTHOR(4, "其他");

    private final int code;
    private final String label;

    UserTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static UserTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (UserTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        UserTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        UserTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        UserTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
