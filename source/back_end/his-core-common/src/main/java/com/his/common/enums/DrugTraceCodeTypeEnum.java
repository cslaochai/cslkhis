package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 药品追溯码码制枚举（1-GS1 2-中国药品追溯码20位 3-其他/未识别）。
 */
@Getter
public enum DrugTraceCodeTypeEnum {

    GS1(1, "GS1"),
    CN20(2, "中国药品追溯码20位"),
    OTHER(3, "其他/未识别");

    private final int code;
    private final String label;

    DrugTraceCodeTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static DrugTraceCodeTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DrugTraceCodeTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DrugTraceCodeTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DrugTraceCodeTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DrugTraceCodeTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
