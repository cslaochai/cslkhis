package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 床位等待状态枚举
 */
@Getter
public enum BedWaitStatusEnum {

    PENDING(0, "等待中"),
    ARRANGED(1, "已安排床位"),
    ADMITTED(2, "已收治入院"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String label;

    BedWaitStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedWaitStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BedWaitStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        BedWaitStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BedWaitStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BedWaitStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
