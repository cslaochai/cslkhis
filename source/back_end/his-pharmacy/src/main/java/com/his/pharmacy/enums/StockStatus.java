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
}
