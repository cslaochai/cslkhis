package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 处方公示状态枚举
 */
@Getter
public enum PublicityStatusEnum {

    PUBLISHED(1, "已公示"),
    NOT_PUBLISHED(0, "未公示");

    private final int code;
    private final String label;

    PublicityStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PublicityStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PublicityStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PublicityStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PublicityStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PublicityStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
