package com.his.common.enums;

import lombok.Getter;

/**
 * 库存状态枚举
 */
@Getter
public enum StockStatusEnum {

    NORMAL(1, "正常"),
    WARNING(2, "预警"),
    OUT_OF_STOCK(3, "缺货"),
    EXPIRED(4, "过期");

    private final int code;
    private final String label;

    StockStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 根据 code 获取对应的枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举，未找到返回 null
     */
    public static StockStatusEnum fromCode(int code) {
        for (StockStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}