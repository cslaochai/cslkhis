package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 医保扣款损失承担方式枚举（1-院方承担 2-科室承担 3-个人承担 4-科室+个人共担）
 */
@Getter
public enum YbLossBearTypeEnum {

    HOSPITAL(1, "院方承担"),
    DEPT(2, "科室承担"),
    EMPLOYEE(3, "个人承担"),
    DEPT_EMPLOYEE(4, "科室+个人共担");

    private final int code;
    private final String label;

    YbLossBearTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static YbLossBearTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (YbLossBearTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        YbLossBearTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        YbLossBearTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        YbLossBearTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
