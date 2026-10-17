package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 用血审核签发级别
 */
@Getter
public enum TransfusionApproveLevelEnum {

    SENIOR_DOCTOR(1, "上级医师"),
    DEPT_HEAD(2, "科主任"),
    MEDICAL_AFFAIRS(3, "医务科");

    private final Integer code;
    private final String label;

    TransfusionApproveLevelEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static TransfusionApproveLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TransfusionApproveLevelEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TransfusionApproveLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TransfusionApproveLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TransfusionApproveLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
