package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum InsRecordStatusEnum {

    REGISTERED(1, "已登记"),
    SIGNED_IN(2, "已签到"),
    CHECKING(3, "检查中"),
    RESULTED(4, "已出结果"),
    REVIEWED(5, "已审核"),
    PUBLISHED(6, "已发布"),
    CANCELLED(7, "已取消");

    private final Integer code;
    private final String desc;

    InsRecordStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code.equals(this.code);
    }

    public static InsRecordStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InsRecordStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InsRecordStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InsRecordStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        InsRecordStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}