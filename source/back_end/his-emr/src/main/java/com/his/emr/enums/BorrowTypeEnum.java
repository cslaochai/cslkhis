package com.his.emr.enums;

import lombok.Getter;

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

    public static BorrowTypeEnum fromCode(int code) {
        for (BorrowTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        BorrowTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
