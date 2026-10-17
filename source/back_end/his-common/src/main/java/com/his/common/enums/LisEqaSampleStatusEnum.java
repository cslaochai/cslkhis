package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 室间质评盲样流转状态枚举（0-待检测 1-已检测 2-已上报 3-已回报）
 */
@Getter
public enum LisEqaSampleStatusEnum {

    PENDING_TEST(0, "待检测"),
    TESTED(1, "已检测"),
    REPORTED(2, "已上报"),
    RETURNED(3, "已回报");

    private final int code;
    private final String label;

    LisEqaSampleStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static LisEqaSampleStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (LisEqaSampleStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        LisEqaSampleStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        LisEqaSampleStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        LisEqaSampleStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
