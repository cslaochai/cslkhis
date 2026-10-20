package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 满意度发放来源枚举
 */
@Getter
public enum SurveySourceEnum {

    FOLLOWUP(1, "随访任务"),
    DISCHARGE(2, "出院结算"),
    MANUAL(3, "人工补发");

    private final int code;
    private final String label;

    SurveySourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SurveySourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SurveySourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        SurveySourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SurveySourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        SurveySourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
