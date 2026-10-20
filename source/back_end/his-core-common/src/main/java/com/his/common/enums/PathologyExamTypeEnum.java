package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病理检查类型枚举（1-常规石蜡 2-术中冰冻 3-细胞学 4-免疫组化 5-疑难会诊）
 */
@Getter
public enum PathologyExamTypeEnum {

    PARAFFIN(1, "常规石蜡"),
    FROZEN(2, "术中冰冻"),
    CYTOLOGY(3, "细胞学"),
    IMMUNO(4, "免疫组化"),
    CONSULTATION(5, "疑难会诊");

    private final int code;
    private final String label;

    PathologyExamTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static PathologyExamTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PathologyExamTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PathologyExamTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PathologyExamTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PathologyExamTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
