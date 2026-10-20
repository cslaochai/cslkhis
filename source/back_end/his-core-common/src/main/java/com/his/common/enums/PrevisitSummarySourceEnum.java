package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 预问诊病史摘要来源枚举
 */
@Getter
public enum PrevisitSummarySourceEnum {

    MODEL(1, "模型"),
    RULE(2, "规则");

    private final int code;
    private final String label;

    PrevisitSummarySourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PrevisitSummarySourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PrevisitSummarySourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PrevisitSummarySourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PrevisitSummarySourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PrevisitSummarySourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
