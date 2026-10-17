package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 护理质控指标事实来源枚举
 */
@Getter
public enum QcIndicatorSourceEnum {

    CHECK(1, "检查表"),
    FACT(2, "不良事件+住院事实");

    private final int code;
    private final String label;

    QcIndicatorSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static QcIndicatorSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (QcIndicatorSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        QcIndicatorSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        QcIndicatorSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        QcIndicatorSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
