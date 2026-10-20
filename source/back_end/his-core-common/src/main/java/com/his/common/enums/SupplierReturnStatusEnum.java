package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 药品供应商退货单状态枚举（药品供应商退货单的状态列，sql/154）
 */
@Getter
public enum SupplierReturnStatusEnum {

    PENDING(1, "待退货"),
    DONE(2, "已退货"),
    CANCELLED(3, "已作废");

    private final int code;
    private final String label;

    SupplierReturnStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static SupplierReturnStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SupplierReturnStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        SupplierReturnStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SupplierReturnStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        SupplierReturnStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
