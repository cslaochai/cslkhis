package com.his.common.enums;

import lombok.Getter;

/**
 * 药品调拨单状态枚举（药品调拨单的状态列，sql/154）
 *
 * <p>发出与接收之间是<b>在途</b>：药既不在药房也不在药库。做成一步搬完省事，
 * 但那样就表达不出"车上那箱货此刻盘不到"，两边库管员也无法各自追责。
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

    public static String labelOf(Integer code) {
        DrugTransferStatusEnum status = fromCode(code);
        return status == null ? "未知(" + code + ")" : status.getLabel();
    }
}
