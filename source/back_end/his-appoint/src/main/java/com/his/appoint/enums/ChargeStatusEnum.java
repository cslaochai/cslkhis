package com.his.appoint.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 收费状态枚举
 */
@Getter
@AllArgsConstructor
public enum ChargeStatusEnum {

    /**
     * 1-待收费
     */
    PENDING(1, "待收费"),

    /**
     * 2-已收费
     */
    PAID(2, "已收费"),

    /**
     * 3-已退费
     */
    REFUNDED(3, "已退费"),

    /**
     * 4-部分退费
     */
    PARTIAL_REFUNDED(4, "部分退费"),

    /**
     * 5-已取消
     */
    CANCELLED(5, "已取消"),

    /**
     * 未知状态（兜底处理，防止反序列化或解析时抛出异常）
     */
    UNKNOWN(0, "未知状态");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String label;

    /**
     * 根据状态码获取对应的枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举实例，若未匹配则返回 UNKNOWN
     */
    public static ChargeStatusEnum fromCode(int code) {
        for (ChargeStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        // 未匹配到有效状态码时，返回 UNKNOWN 而不是 null，增强系统健壮性
        return UNKNOWN;
    }
}