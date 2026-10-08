package com.his.common.enums;

import lombok.Getter;

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

    public static SupplierReturnStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SupplierReturnStatusEnum status : values()) {
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
        SupplierReturnStatusEnum status = fromCode(code);
        return status == null ? "未知(" + code + ")" : status.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        SupplierReturnStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
