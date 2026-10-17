package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 营养筛查量表枚举
 */
@Getter
public enum NutritionScreenTypeEnum {

    NRS2002(1, "NRS2002 营养风险筛查"),
    PG_SGA(2, "PG-SGA 患者参与主观整体评估"),
    MNA(3, "MNA 老年微型营养评估");

    private final int code;
    private final String label;

    NutritionScreenTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static NutritionScreenTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NutritionScreenTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        NutritionScreenTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NutritionScreenTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        NutritionScreenTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
