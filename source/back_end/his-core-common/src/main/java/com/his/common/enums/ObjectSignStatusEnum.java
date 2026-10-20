package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 被签对象（病历/医嘱）行上的签名锚点状态。
 */
public enum ObjectSignStatusEnum {

    UNSIGNED(0, "未签名"),
    SIGNED(1, "已签名"),
    INVALIDATED(2, "签名已失效");

    private final int code;
    private final String label;

    ObjectSignStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ObjectSignStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ObjectSignStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        ObjectSignStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ObjectSignStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
