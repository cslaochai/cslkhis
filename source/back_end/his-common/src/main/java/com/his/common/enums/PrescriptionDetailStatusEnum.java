package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 处方明细状态枚举
 */
@Getter
public enum PrescriptionDetailStatusEnum {

    NORMAL(1, "正常"),
    DISPENSED(2, "已发药"),
    RETURNED(3, "已退药");

    private final int code;
    private final String label;

    PrescriptionDetailStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PrescriptionDetailStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PrescriptionDetailStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PrescriptionDetailStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PrescriptionDetailStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PrescriptionDetailStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
