package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 不良事件来源枚举（sql/168，落在不良事件上报的获得方式列）
 */
@Getter
public enum AdverseAcquiredEnum {

    /**
     * 院内获得（进发生率分子）
     */
    HOSPITAL_ACQUIRED(1, "院内获得"),
    /**
     * 入院带入（留档不进分子）
     */
    ADMITTED_WITH(2, "入院带入");

    private final int code;
    private final String label;

    AdverseAcquiredEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AdverseAcquiredEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AdverseAcquiredEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AdverseAcquiredEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AdverseAcquiredEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AdverseAcquiredEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
