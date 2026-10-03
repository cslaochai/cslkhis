package com.his.emr.enums;

import lombok.Getter;

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

    public static SurveySceneEnum fromCode(int code) {
        for (SurveySceneEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        SurveySceneEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
