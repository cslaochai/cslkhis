package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病危重通知状态枚举
 */
@Getter
public enum NoticeStatusEnum {

    DRAFT(1, "草稿"),
    ISSUED(2, "已签发"),
    ACKED(3, "已签收"),
    VOIDED(4, "已作废");

    private final int code;
    private final String label;

    NoticeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NoticeStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NoticeStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        NoticeStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NoticeStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        NoticeStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
