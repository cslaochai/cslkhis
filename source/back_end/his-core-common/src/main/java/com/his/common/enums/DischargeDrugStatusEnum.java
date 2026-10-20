package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 出院带药发药状态枚举
 */
@Getter
public enum DischargeDrugStatusEnum {

    PENDING(1, "待发药"),
    DISPENSED(2, "已发药");

    private final int code;
    private final String label;

    DischargeDrugStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DischargeDrugStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DischargeDrugStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DischargeDrugStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DischargeDrugStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DischargeDrugStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
