package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 药品调拨单状态枚举（药品调拨单的状态列，sql/154）
 */
@Getter
public enum DrugTransferStatusEnum {

    PENDING_OUT(1, "待发出"),
    PENDING_IN(2, "待接收"),
    DONE(3, "已完成"),
    CANCELLED(4, "已作废");

    private final int code;
    private final String label;

    DrugTransferStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static DrugTransferStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DrugTransferStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DrugTransferStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DrugTransferStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DrugTransferStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
