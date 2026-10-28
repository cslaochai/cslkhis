package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 参保人参保状态：医保联网通道回执里的参保状态码值，由医保局端参保库维护，院内只读不写。
 */
@Getter
public enum InsuredPersonStatusEnum {

    STOPPED(0, "停保"),
    NORMAL(1, "正常参保");

    private final Integer code;
    private final String label;

    InsuredPersonStatusEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InsuredPersonStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InsuredPersonStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    /** 参保是否正常：只有明确回 1-正常参保 才算，null/0/脏值一律否（fail-closed，宁可降自费不可放行医保） */
    public static boolean isNormal(Integer code) {
        return NORMAL.code.equals(code);
    }

    /** 展示：合法→label；null/脏值→空串 */
    public static String getLabel(Integer code) {
        InsuredPersonStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    /** 展示：null/脏值→调用方缺省文案 */
    public static String getLabel(Integer code, String defaultValue) {
        InsuredPersonStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    /** 异常/审计：null/脏值→未知(n)，保留原始码值便于排查 */
    public static String getLabelOrUnknown(Integer code) {
        InsuredPersonStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
