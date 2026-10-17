package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 透析频次枚举
 */
@Getter
public enum DialysisFreqEnum {

    WEEK_1(1, "每周1次"),
    WEEK_2(2, "每周2次"),
    WEEK_3(3, "每周3次"),
    WEEK_4_PLUS(4, "每周≥4次");

    private final int code;
    private final String label;

    DialysisFreqEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DialysisFreqEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DialysisFreqEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DialysisFreqEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DialysisFreqEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DialysisFreqEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
