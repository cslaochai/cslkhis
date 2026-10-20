package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * I 类切口预防用药点评结论枚举（合理 / 不合理）。
 */
@Getter
public enum IncisionReviewResultEnum {

    REASONABLE(1, "合理"),
    UNREASONABLE(2, "不合理");

    private final int code;
    private final String label;

    IncisionReviewResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static IncisionReviewResultEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (IncisionReviewResultEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        IncisionReviewResultEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        IncisionReviewResultEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        IncisionReviewResultEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
