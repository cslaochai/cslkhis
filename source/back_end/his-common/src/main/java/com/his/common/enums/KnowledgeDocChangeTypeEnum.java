package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 知识库文档历史留痕的变更类型枚举（1-新建 2-编辑 3-回退）
 */
@Getter
public enum KnowledgeDocChangeTypeEnum {

    CREATE(1, "新建"),
    EDIT(2, "编辑"),
    REVERT(3, "回退");

    private final int code;
    private final String label;

    KnowledgeDocChangeTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static KnowledgeDocChangeTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (KnowledgeDocChangeTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        KnowledgeDocChangeTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        KnowledgeDocChangeTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        KnowledgeDocChangeTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
