package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 值班日志/交班本状态枚举。
 */
@Getter
public enum DutyLogStatusEnum {

    PENDING(0, "待处理"),
    DONE(1, "已处理"),
    HANDED(2, "已交班"),
    ACKED(3, "已签收");

    private final int code;
    private final String label;

    DutyLogStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }

    public static DutyLogStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DutyLogStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DutyLogStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DutyLogStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DutyLogStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
