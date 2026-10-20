package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病理单状态枚举（码值口径 = 病理申请单状态列的库内注释）。
 */
@Getter
public enum PathologyStatusEnum {

    REGISTERED(1, "已登记"),
    RECEIVED(2, "已接收"),
    SAMPLED(3, "已取材"),
    SLICED(4, "已切片"),
    REPORTED(5, "已出报告"),
    AUDITED(6, "已审核"),
    PUBLISHED(7, "已发布"),
    CANCELLED(8, "已取消");

    private final int code;
    private final String label;

    PathologyStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }

    public static PathologyStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PathologyStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PathologyStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PathologyStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PathologyStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
