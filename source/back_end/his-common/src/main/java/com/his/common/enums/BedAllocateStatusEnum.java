package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 床位分配状态枚举
 */
@Getter
public enum BedAllocateStatusEnum {

    RESERVED(1, "已预留"),
    ADMITTED(2, "已转入院"),
    RELEASED(3, "已释放"),
    VOID(4, "已作废");

    private final int code;
    private final String label;

    BedAllocateStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedAllocateStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BedAllocateStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        BedAllocateStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BedAllocateStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BedAllocateStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
