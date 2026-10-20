package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 合格性判定结果（1-合格 2-不合格），全仓「合格 / 不合格」这一档判定的唯一枚举。
 */
@Getter
public enum QualifiedResultEnum {

    OK(1, "合格"),
    NG(2, "不合格");

    private final int code;
    private final String label;

    QualifiedResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
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

    public static QualifiedResultEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (QualifiedResultEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        QualifiedResultEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        QualifiedResultEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        QualifiedResultEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
