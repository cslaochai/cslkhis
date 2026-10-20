package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 手术清点物品类别
 */
@Getter
public enum CountItemCategoryEnum {

    INSTRUMENT(1, "器械"),
    DRESSING(2, "敷料"),
    NEEDLE(3, "缝针"),
    BLADE(4, "刀片"),
    OTHER(5, "其他");

    private final Integer code;
    private final String label;

    CountItemCategoryEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static CountItemCategoryEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CountItemCategoryEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CountItemCategoryEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CountItemCategoryEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CountItemCategoryEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
