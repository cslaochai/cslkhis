package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 远程会诊状态枚举
 */
@Getter
public enum TeleConsultStatusEnum {

    PENDING(1, "待安排"),
    ARRANGED(2, "已安排"),
    DONE(3, "已完成"),
    CANCELED(4, "已取消");

    private final int code;
    private final String label;

    TeleConsultStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TeleConsultStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TeleConsultStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TeleConsultStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TeleConsultStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TeleConsultStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
