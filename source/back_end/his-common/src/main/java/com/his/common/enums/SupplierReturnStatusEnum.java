package com.his.common.enums;

import lombok.Getter;

/**
 * 药品供应商退货单状态枚举（药品供应商退货单的状态列，sql/154）
 *
 * <p>与调拨单不同，退货只有「扣一次库存」这一个动作（药离开医院，没有院内接收方），
 * 所以没有待接收这一态。码值与 {@code his_supplier_return_status} 字典一致。
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

    public static String labelOf(Integer code) {
        SupplierReturnStatusEnum status = fromCode(code);
        return status == null ? "未知(" + code + ")" : status.getLabel();
    }
}
