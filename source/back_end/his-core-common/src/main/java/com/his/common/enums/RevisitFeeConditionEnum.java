package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 复诊收费策略的三态匹配条件（0-不限 1-要求相同 2-要求不同），
 * 「是否要求与原就诊同一医生」「是否要求与原就诊同一科室」两个条件共用这一套码值，
 * 具体比的是医生还是科室由策略行自己说明。
 */
@Getter
public enum RevisitFeeConditionEnum {

    ANY(0, "不限"),
    SAME(1, "要求相同"),
    DIFF(2, "要求不同");

    private final int code;
    private final String label;

    RevisitFeeConditionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static RevisitFeeConditionEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RevisitFeeConditionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RevisitFeeConditionEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RevisitFeeConditionEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RevisitFeeConditionEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
