package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 投诉人与患者关系枚举
 */
@Getter
public enum ComplainantRelEnum {

    SELF(1, "本人"),
    FAMILY(2, "家属"),
    AGENT(3, "代理人"),
    OTHER(4, "其他");

    private final int code;
    private final String label;

    ComplainantRelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ComplainantRelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ComplainantRelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ComplainantRelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ComplainantRelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ComplainantRelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
