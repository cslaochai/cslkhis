package com.his.emr.enums;

import lombok.Getter;

/**
 * 满意度试题题型枚举
 */
@Getter
public enum SurveyQuestionTypeEnum {

    SCALE(1, "量表"),
    SINGLE(2, "单选"),
    MULTI(3, "多选"),
    NPS(4, "NPS推荐度"),
    TEXT(5, "开放文本");

    private final int code;
    private final String label;

    SurveyQuestionTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SurveyQuestionTypeEnum fromCode(int code) {
        for (SurveyQuestionTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        SurveyQuestionTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        SurveyQuestionTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
