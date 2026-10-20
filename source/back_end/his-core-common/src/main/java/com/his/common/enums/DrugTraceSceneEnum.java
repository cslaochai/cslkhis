package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 药品追溯码扫码场景枚举（1-入库采集 2-发药核销）。
 */
@Getter
public enum DrugTraceSceneEnum {

    COLLECT(1, "入库采集"),
    DISPENSE(2, "发药核销");

    private final int code;
    private final String label;

    DrugTraceSceneEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static DrugTraceSceneEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DrugTraceSceneEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DrugTraceSceneEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DrugTraceSceneEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DrugTraceSceneEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
