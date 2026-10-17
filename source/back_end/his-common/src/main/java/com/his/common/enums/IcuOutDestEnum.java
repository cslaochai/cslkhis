package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * ICU 转出去向枚举
 */
@Getter
public enum IcuOutDestEnum {

    NORMAL_WARD(1, "普通病房"),
    SPECIAL_WARD(2, "专科病房"),
    OPERATION_ROOM(3, "手术室"),
    TRANSFER_HOSPITAL(4, "转院"),
    DEATH(5, "死亡"),
    SELF_DISCHARGE(6, "自动离院");

    private final int code;
    private final String label;

    IcuOutDestEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static IcuOutDestEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (IcuOutDestEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        IcuOutDestEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        IcuOutDestEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        IcuOutDestEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
