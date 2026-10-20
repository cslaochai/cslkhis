package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 转诊状态枚举
 */
@Getter
public enum ReferralStatusEnum {

    PENDING(0, "待确认"),
    CONFIRMED(1, "已确认"),
    FINISHED(2, "已完成"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String label;

    ReferralStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ReferralStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ReferralStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ReferralStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ReferralStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ReferralStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
