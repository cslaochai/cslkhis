package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 满意度场景枚举
 */
@Getter
public enum SurveySceneEnum {

    DISCHARGE_FOLLOWUP(1, "出院随访"),
    OUTPATIENT(2, "门诊"),
    INPATIENT(3, "住院在院"),
    CHECKUP(4, "体检");

    private final int code;
    private final String label;

    SurveySceneEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SurveySceneEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SurveySceneEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        SurveySceneEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SurveySceneEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        SurveySceneEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
