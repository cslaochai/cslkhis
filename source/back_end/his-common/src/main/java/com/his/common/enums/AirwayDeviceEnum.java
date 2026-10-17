package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 气道管理方式（0-无 1-气管插管 2-喉罩 3-面罩 4-其他）。
 */
@Getter
public enum AirwayDeviceEnum {

    NONE(0, "无（保留自主呼吸）"),
    ENDOTRACHEAL_TUBE(1, "气管插管"),
    LMA(2, "喉罩"),
    MASK(3, "面罩"),
    OTHER(4, "其他");

    private final int code;
    private final String label;

    AirwayDeviceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AirwayDeviceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AirwayDeviceEnum item : values()) {
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
        AirwayDeviceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AirwayDeviceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AirwayDeviceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
