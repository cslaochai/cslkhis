package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * VTE 事件类型枚举（文案与前端 src/lib/vte.js 逐字对齐，改这边必须改那边）
 */
@Getter
public enum VteEventTypeEnum {

    DVT(1, "深静脉血栓（DVT）"),
    PE(2, "肺栓塞（PE）"),
    BLEED(3, "预防相关出血");

    private final int code;
    private final String label;

    VteEventTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static VteEventTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (VteEventTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        VteEventTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        VteEventTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        VteEventTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
