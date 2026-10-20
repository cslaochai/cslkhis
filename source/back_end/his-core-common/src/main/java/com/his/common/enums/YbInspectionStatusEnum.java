package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 医保飞检批次状态枚举（1-进行中 2-已结项 3-已作废）
 */
@Getter
public enum YbInspectionStatusEnum {

    RUNNING(1, "进行中"),
    CONCLUDED(2, "已结项"),
    CANCELLED(3, "已作废");

    private final int code;
    private final String label;

    YbInspectionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static YbInspectionStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (YbInspectionStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        YbInspectionStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        YbInspectionStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        YbInspectionStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
