package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 在线问诊状态枚举
 */
@Getter
public enum OnlineConsultStatusEnum {

    WAITING(1, "待接诊"),
    ACCEPTED(2, "接诊中"),
    DONE(3, "已完成"),
    REJECTED(4, "已退诊");

    private final int code;
    private final String label;

    OnlineConsultStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OnlineConsultStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (OnlineConsultStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        OnlineConsultStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        OnlineConsultStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        OnlineConsultStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
