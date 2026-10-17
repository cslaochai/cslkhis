package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 技术授权状态枚举
 */
@Getter
public enum TechAuthStatusEnum {

    /**
     * 待审批：科室已提交，委员会未批，闸门不认
     */
    PENDING(1, "待审批"),
    /**
     * 已授权：唯一放行的状态
     */
    GRANTED(2, "已授权"),
    /**
     * 已驳回：审批未通过
     */
    REJECTED(3, "已驳回"),
    /**
     * 已收回：动态调整 / 年度再授权未通过 / 事件触发
     */
    REVOKED(4, "已收回");

    private final int code;
    private final String label;

    TechAuthStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static TechAuthStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TechAuthStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TechAuthStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TechAuthStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TechAuthStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
