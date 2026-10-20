package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static SurveyDimensionEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SurveyDimensionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        SurveyDimensionEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SurveyDimensionEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        SurveyDimensionEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}