package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 满意度答卷状态枚举
 */
@Getter
public enum AnswerStatusEnum {

    VALID(1, "有效"),
    VOID(2, "已作废");

    private final int code;
    private final String label;

    AnswerStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AnswerStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AnswerStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AnswerStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AnswerStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AnswerStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
