package com.his.emr.enums;

import lombok.Getter;

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

    public static BorrowStatusEnum fromCode(int code) {
        for (BorrowStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        BorrowStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
