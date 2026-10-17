package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 配血单状态枚举
 */
@Getter
public enum CrossmatchOrderStatusEnum {

    PENDING(1, "待配血"),
    MATCHED(2, "已配血"),
    VERIFIED(3, "已复核"),
    VOIDED(4, "已作废");

    private final int code;
    private final String label;

    CrossmatchOrderStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static CrossmatchOrderStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CrossmatchOrderStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CrossmatchOrderStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CrossmatchOrderStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CrossmatchOrderStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
