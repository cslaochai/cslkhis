package com.his.common.enums;

import lombok.Getter;

/**
 * 门诊处方缴费状态（处方主表的缴费状态列）
 */
@Getter
public enum PrescriptionPayStatusEnum {

    UNPAID(0, "未缴费"),
    PAID(1, "已缴费"),
    REFUNDED(2, "已退费");

    private final int code;
    private final String label;

    PrescriptionPayStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PrescriptionPayStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PrescriptionPayStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        PrescriptionPayStatusEnum status = fromCode(code);
        return status == null ? null : status.getLabel();
    }
}
