package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病案借阅状态枚举
 */
@Getter
public enum BorrowStatusEnum {

    PENDING(1, "待审核"),
    LENT(2, "已借出"),
    RETURNED(3, "已归还"),
    REJECTED(4, "已拒绝"),
    COPIED(5, "已复印");

    private final int code;
    private final String label;

    BorrowStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BorrowStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BorrowStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        BorrowStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BorrowStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BorrowStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
