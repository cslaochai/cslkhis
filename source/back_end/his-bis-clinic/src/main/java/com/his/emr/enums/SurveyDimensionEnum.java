package com.his.emr.enums;

import lombok.Getter;

/**
 * 满意度评价维度枚举（码值口径 = 建表 SQL 的列注释 / 字典 his_survey_dimension）。
 */
@Getter
public enum SurveyDimensionEnum {

    REGISTRATION(1, "挂号便捷"),
    DOCTOR_SERVICE(2, "医生服务"),
    NURSE_SERVICE(3, "护士服务"),
    ENVIRONMENT_FLOW(4, "环境与流程"),
    FEE_TRANSPARENT(5, "费用透明"),
    EFFECT_AND_SAFETY(6, "疗效与安全感"),
    OVERALL_IMPRESSION(7, "总体印象");

    private final int code;
    private final String label;

    SurveyDimensionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SurveyDimensionEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SurveyDimensionEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用：null 或脏值返回空串（不把「未知」渲染给用户看）
     */
    public static String getText(Integer code) {
        SurveyDimensionEnum e = fromCode(code);
        return e == null ? "" : e.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」，保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        SurveyDimensionEnum e = fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }
}