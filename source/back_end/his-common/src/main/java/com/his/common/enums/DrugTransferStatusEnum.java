package com.his.common.enums;

import lombok.Getter;

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

    public static DrugTransferStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DrugTransferStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        DrugTransferStatusEnum status = fromCode(code);
        return status == null ? "未知(" + code + ")" : status.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        DrugTransferStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
