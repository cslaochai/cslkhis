package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 住院请假状态枚举
 */
@Getter
public enum LeaveStatusEnum {

    PENDING(1, "待审批"),
    APPROVED(2, "已批准"),
    LEFT(3, "已离院"),
    RETURNED(4, "已返回"),
    REJECTED(5, "已拒绝"),
    CANCELLED(6, "已取消");

    private final int code;
    private final String label;

    LeaveStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static LeaveStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (LeaveStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        LeaveStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        LeaveStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        LeaveStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
