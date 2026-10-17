package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 手术麻醉计费状态（0-未计费 1-已计费 2-计费失败）。
 */
@Getter
public enum AnesthesiaChargeStatusEnum {

    PENDING(0, "未计费"),
    DONE(1, "已计费"),
    FAILED(2, "计费失败");

    private final int code;
    private final String label;

    AnesthesiaChargeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AnesthesiaChargeStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AnesthesiaChargeStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        AnesthesiaChargeStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AnesthesiaChargeStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AnesthesiaChargeStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
