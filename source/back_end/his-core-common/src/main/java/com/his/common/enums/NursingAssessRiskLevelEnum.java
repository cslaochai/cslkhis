package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 护理风险评估等级枚举（1-低风险 2-中风险 3-高风险 4-极高风险）。
 * <p>
 * 等级由评估总分按分数段现算，3 及以上才算「高风险」，交班与预警口径都取这一档。
 */
@Getter
public enum NursingAssessRiskLevelEnum {

    LOW(1, "低风险"),
    MEDIUM(2, "中风险"),
    HIGH(3, "高风险"),
    EXTREME(4, "极高风险");

    private final int code;
    private final String label;

    NursingAssessRiskLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static NursingAssessRiskLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NursingAssessRiskLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        NursingAssessRiskLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NursingAssessRiskLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        NursingAssessRiskLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
