package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 透析器类型枚举
 */
@Getter
public enum DialyzerTypeEnum {

    LOW_FLUX_CELLULOSE(1, "低通量纤维素膜"),
    LOW_FLUX_SYNTHETIC(2, "低通量合成膜"),
    HIGH_FLUX_SYNTHETIC(3, "高通量合成膜");

    private final int code;
    private final String label;

    DialyzerTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DialyzerTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DialyzerTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DialyzerTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DialyzerTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DialyzerTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
