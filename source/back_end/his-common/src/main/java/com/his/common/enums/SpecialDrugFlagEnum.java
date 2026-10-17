package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 特殊管理药品分类枚举
 */
@Getter
public enum SpecialDrugFlagEnum {

    NORMAL(0, "普通"),
    NARCOTIC(1, "麻醉药品"),
    PSYCHOTROPIC_1(2, "第一类精神药品"),
    PSYCHOTROPIC_2(3, "第二类精神药品"),
    TOXIC(4, "毒性药品");

    private final int code;
    private final String label;

    SpecialDrugFlagEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SpecialDrugFlagEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SpecialDrugFlagEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        SpecialDrugFlagEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SpecialDrugFlagEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        SpecialDrugFlagEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
