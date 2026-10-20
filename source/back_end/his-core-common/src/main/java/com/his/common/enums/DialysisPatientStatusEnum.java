package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 透析档案状态枚举
 */
@Getter
public enum DialysisPatientStatusEnum {

    ON(1, "在透"),
    PAUSED(2, "暂停"),
    EXITED(3, "退出");

    private final int code;
    private final String label;

    DialysisPatientStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DialysisPatientStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DialysisPatientStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DialysisPatientStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DialysisPatientStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DialysisPatientStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
