package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 透析抗凝方式枚举
 */
@Getter
public enum DialysisAnticoagulantEnum {

    HEPARIN(1, "普通肝素"),
    LMWH(2, "低分子肝素"),
    CITRATE(3, "枸橼酸钠"),
    NONE(4, "无肝素");

    private final int code;
    private final String label;

    DialysisAnticoagulantEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DialysisAnticoagulantEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DialysisAnticoagulantEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DialysisAnticoagulantEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DialysisAnticoagulantEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DialysisAnticoagulantEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
