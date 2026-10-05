package com.his.common.enums;

import lombok.Getter;

/**
 * 计费状态枚举
 */
@Getter
public enum BillingStatusEnum {

    UNBILLED(0, "未计费"),
    BILLED(1, "已计费"),
    FAILED(2, "计费失败"),
    NO_NEED(3, "无需计费");

    private final int code;
    private final String label;

    BillingStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BillingStatusEnum fromCode(int code) {
        for (BillingStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        BillingStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
