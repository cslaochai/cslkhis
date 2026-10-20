package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病案获取类型枚举
 */
@Getter
public enum BorrowTypeEnum {

    BORROW(1, "借阅"),
    COPY(2, "复印");

    private final int code;
    private final String label;

    BorrowTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BorrowTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BorrowTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        BorrowTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BorrowTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BorrowTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
