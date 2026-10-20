package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 临床规则核查处理状态枚举
 */
@Getter
public enum RuleCheckStatusEnum {

    PENDING(1, "待处理"),
    HANDLED(2, "已处理"),
    IGNORED(3, "已忽略");

    private final int code;
    private final String label;

    RuleCheckStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RuleCheckStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RuleCheckStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RuleCheckStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RuleCheckStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RuleCheckStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
