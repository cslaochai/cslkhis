package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 知识文档来源类型枚举（1-内置示例 2-手工录入 3-文件导入）
 */
@Getter
public enum KnowledgeDocSourceTypeEnum {

    BUILTIN_SAMPLE(1, "内置示例"),
    MANUAL_ENTRY(2, "手工录入"),
    FILE_IMPORT(3, "文件导入");

    private final int code;
    private final String label;

    KnowledgeDocSourceTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static KnowledgeDocSourceTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (KnowledgeDocSourceTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        KnowledgeDocSourceTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        KnowledgeDocSourceTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        KnowledgeDocSourceTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
