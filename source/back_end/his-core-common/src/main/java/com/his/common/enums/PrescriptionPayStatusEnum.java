package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static PrescriptionPayStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PrescriptionPayStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PrescriptionPayStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PrescriptionPayStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PrescriptionPayStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
