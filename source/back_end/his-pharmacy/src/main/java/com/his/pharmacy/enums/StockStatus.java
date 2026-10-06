package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 药品库存状态枚举
 */
@Getter
public enum StockStatus {

    NORMAL(1, "正常"),
    WARNING(2, "预警"),
    OUT_OF_STOCK(3, "缺货"),
    EXPIRED(4, "过期");

    private final int code;
    private final String label;

    StockStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StockStatus fromCode(int code) {
        for (StockStatus status : values()) {
            if (status.code == code) return status;
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /** 展示用：null 或脏值返回空串（不把「未知」渲染给用户看） */
    public static String getText(Integer code) {
        StockStatus item = code == null ? null : fromCode(code);
        return item == null ? "" : item.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」，保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        StockStatus item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
