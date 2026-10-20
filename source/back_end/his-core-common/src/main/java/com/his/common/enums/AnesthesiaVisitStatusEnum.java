package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 麻醉术前访视单状态（0-草稿 1-已完成）。
 */
@Getter
public enum AnesthesiaVisitStatusEnum {

    DRAFT(0, "草稿"),
    FINISHED(1, "已完成");

    private final int code;
    private final String label;

    AnesthesiaVisitStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AnesthesiaVisitStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AnesthesiaVisitStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        AnesthesiaVisitStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AnesthesiaVisitStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AnesthesiaVisitStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
