package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 床位状态枚举
 */
@Getter
public enum BedStatusEnum {

    REPAIR(0, "维修"),
    FREE(1, "空闲"),
    OCCUPIED(2, "占用"),
    LOCKED(3, "锁定");

    private final int code;
    private final String label;

    BedStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BedStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        BedStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BedStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BedStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
