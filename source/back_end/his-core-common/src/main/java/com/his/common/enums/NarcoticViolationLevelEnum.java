package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 麻精处方违规级别（BLOCK-拦截 WARN-警告）
 */
@Getter
public enum NarcoticViolationLevelEnum {

    BLOCK("BLOCK", "拦截"),
    WARN("WARN", "警告");

    private final String code;
    private final String label;

    NarcoticViolationLevelEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static NarcoticViolationLevelEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (NarcoticViolationLevelEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        NarcoticViolationLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        NarcoticViolationLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        NarcoticViolationLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
