package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 麻醉术前访视结论（1-可施行麻醉 2-暂缓手术 3-需会诊/进一步评估）。
 */
@Getter
public enum VisitConclusionEnum {

    OK(1, "可施行麻醉"),
    HOLD(2, "暂缓手术"),
    CONSULT(3, "需会诊/进一步评估");

    private final int code;
    private final String label;

    VisitConclusionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static VisitConclusionEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (VisitConclusionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        VisitConclusionEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        VisitConclusionEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        VisitConclusionEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
