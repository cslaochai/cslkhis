package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 血库血袋状态枚举
 */
@Getter
public enum BloodInventoryStatusEnum {

    IN_STOCK(1, "在库"),
    RESERVED(2, "已预留"),
    ISSUED(3, "已发血"),
    SCRAPPED(4, "已报废"),
    RETURNED(5, "已退回");

    private final int code;
    private final String label;

    BloodInventoryStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static BloodInventoryStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BloodInventoryStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        BloodInventoryStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BloodInventoryStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BloodInventoryStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
