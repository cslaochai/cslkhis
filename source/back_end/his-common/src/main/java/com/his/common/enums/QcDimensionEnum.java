package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 病历形式质控的三个维度。
 */
public enum QcDimensionEnum {

    /**
     * 完整性：该有的要素有没有
     */
    COMPLETENESS(1, "完整性", "必填要素是否落实到病历原文"),

    /**
     * 规范性：写得合不合规范
     */
    REGULARITY(2, "规范性", "书写格式与用词是否合规"),

    /**
     * 逻辑性：内容之间自不自洽
     */
    LOGIC(3, "逻辑性", "内容与事实、时间、性别年龄之间是否矛盾");

    private final int code;

    private final String label;

    private final String description;

    QcDimensionEnum(int code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public static QcDimensionEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (QcDimensionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        QcDimensionEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        QcDimensionEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        QcDimensionEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
