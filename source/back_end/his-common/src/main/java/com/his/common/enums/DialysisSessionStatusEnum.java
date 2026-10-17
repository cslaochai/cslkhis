package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 透析治疗单状态枚举
 */
@Getter
public enum DialysisSessionStatusEnum {

    SCHEDULED(1, "已排班"),
    ON_MACHINE(2, "透析中"),
    DONE(3, "已完成"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String label;

    DialysisSessionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DialysisSessionStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DialysisSessionStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DialysisSessionStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DialysisSessionStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DialysisSessionStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
