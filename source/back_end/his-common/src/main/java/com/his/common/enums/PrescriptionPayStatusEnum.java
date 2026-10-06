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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        PrescriptionPayStatusEnum status = fromCode(code);
        return status == null ? null : status.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        PrescriptionPayStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
