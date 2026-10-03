package com.his.patient.enums;

import lombok.Getter;

/**
 * 住院转科状态枚举
 */
@Getter
public enum TransferStatusEnum {

    PENDING(0, "待接收"),
    DONE(1, "已完成"),
    CANCELLED(2, "已取消");

    private final int code;
    private final String label;

    TransferStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TransferStatusEnum fromCode(int code) {
        for (TransferStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        TransferStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
