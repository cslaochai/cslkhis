package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 麻醉效果
 */
@Getter
public enum AnesthesiaEffectEnum {

    SATISFACTORY(1, "满意"),
    POOR(2, "欠佳"),
    FAILED(3, "失败改麻醉方式");

    private final Integer code;
    private final String label;

    AnesthesiaEffectEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AnesthesiaEffectEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AnesthesiaEffectEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AnesthesiaEffectEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AnesthesiaEffectEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AnesthesiaEffectEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
