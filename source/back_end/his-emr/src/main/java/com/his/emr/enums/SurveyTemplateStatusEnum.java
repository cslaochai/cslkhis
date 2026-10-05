package com.his.emr.enums;

import lombok.Getter;

/**
 * 满意度模板状态枚举
 */
@Getter
public enum SurveyTemplateStatusEnum {

    ENABLED(1, "启用"),
    DISABLED(2, "停用");

    private final int code;
    private final String label;

    SurveyTemplateStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SurveyTemplateStatusEnum fromCode(int code) {
        for (SurveyTemplateStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        SurveyTemplateStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
