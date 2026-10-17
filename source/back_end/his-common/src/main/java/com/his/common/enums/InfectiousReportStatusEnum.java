package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 传染病报卡状态枚举
 */
@Getter
public enum InfectiousReportStatusEnum {

    PENDING(1, "待审核"),
    AUDITED(2, "已审核待直报"),
    DIRECT(3, "已直报"),
    RETURNED(4, "已退报");

    private final int code;
    private final String label;

    InfectiousReportStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfectiousReportStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InfectiousReportStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InfectiousReportStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InfectiousReportStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        InfectiousReportStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
