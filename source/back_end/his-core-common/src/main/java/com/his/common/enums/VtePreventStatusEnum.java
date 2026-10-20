package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * VTE 预防措施落实状态枚举
 */
@Getter
public enum VtePreventStatusEnum {

    PENDING(0, "待落实"),
    DONE(1, "已落实"),
    CONTRAINDICATION(2, "禁忌未用"),
    REFUSED(3, "患者拒绝");

    private final int code;
    private final String label;

    VtePreventStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static VtePreventStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (VtePreventStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        VtePreventStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        VtePreventStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        VtePreventStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
