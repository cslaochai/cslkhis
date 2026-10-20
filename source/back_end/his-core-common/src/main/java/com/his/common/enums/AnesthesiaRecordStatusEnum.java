package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 麻醉记录单状态（0-记录中 1-已提交 2-已审核）。
 */
@Getter
public enum AnesthesiaRecordStatusEnum {

    DRAFT(0, "记录中"),
    SUBMITTED(1, "已提交"),
    AUDITED(2, "已审核");

    private final int code;
    private final String label;

    AnesthesiaRecordStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AnesthesiaRecordStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AnesthesiaRecordStatusEnum item : values()) {
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
        AnesthesiaRecordStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AnesthesiaRecordStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AnesthesiaRecordStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
