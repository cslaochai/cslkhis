package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 住院摆药明细状态枚举（待配药 / 已配药 / 已核对 / 已退药）。
 * 与发药单口径（待发药/已发药/已退药/已取消）是两条不同列，不可混用。
 */
@Getter
public enum WardDispenseItemStatusEnum {

    PENDING(1, "待配药"),
    DISPENSED(2, "已配药"),
    CHECKED(3, "已核对"),
    RETURNED(4, "已退药");

    private final int code;
    private final String label;

    WardDispenseItemStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static WardDispenseItemStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (WardDispenseItemStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        WardDispenseItemStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        WardDispenseItemStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        WardDispenseItemStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
