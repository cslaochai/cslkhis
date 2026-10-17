package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 医疗技术授权类别枚举
 */
@Getter
public enum TechAuthCategoryEnum {

    /**
     * 手术：排台主刀、手术医嘱、日间手术的准入按此类别
     */
    SURGERY(1, "手术"),
    /**
     * 麻醉：按所伴手术同级授权，排台麻醉医师按此类别
     */
    ANESTHESIA(2, "麻醉"),
    /**
     * 内镜与介入：内镜/介入操作者按此类别（ERCP 等按四级）
     */
    ENDOSCOPY(3, "内镜与介入");

    private final int code;
    private final String label;

    TechAuthCategoryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static TechAuthCategoryEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TechAuthCategoryEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TechAuthCategoryEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TechAuthCategoryEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TechAuthCategoryEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
