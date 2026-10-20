package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 配血结论枚举
 */
@Getter
public enum CrossmatchResultEnum {

    MATCHED(1, "相合"),
    UNMATCHED(2, "不相合"),
    SUSPECT_AGGLUTINATION(3, "可疑凝集");

    private final int code;
    private final String label;

    CrossmatchResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static CrossmatchResultEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CrossmatchResultEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CrossmatchResultEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CrossmatchResultEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CrossmatchResultEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
