package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 纠纷投诉处理状态枚举
 */
@Getter
public enum DisputeStatusEnum {

    PENDING(1, "待受理"),
    INVESTIGATING(2, "调查中"),
    HANDLING(3, "处理中"),
    CLOSED(4, "已结案"),
    REVOKED(5, "已撤销");

    private final int code;
    private final String label;

    DisputeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DisputeStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DisputeStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DisputeStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DisputeStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DisputeStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
