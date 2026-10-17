package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 随访电话外呼状态枚举
 */
@Getter
public enum FollowupCallStatusEnum {

    NONE(0, "未外呼"),
    WAITING(1, "待外呼"),
    CONNECTED(2, "已接通"),
    NO_ANSWER(3, "未接通");

    private final int code;
    private final String label;

    FollowupCallStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static FollowupCallStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (FollowupCallStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        FollowupCallStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        FollowupCallStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        FollowupCallStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
