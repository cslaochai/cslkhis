package com.his.appoint.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 挂号来源枚举
 */
@Getter
@AllArgsConstructor
public enum AppointSourceEnum {

    /**
     * 1-窗口挂号
     */
    WINDOW(1, "窗口挂号"),

    /**
     * 2-自助机挂号
     */
    SELF_SERVICE(2, "自助机挂号"),

    /**
     * 3-网上挂号
     */
    ONLINE(3, "网上挂号"),

    /**
     * 4-预约挂号
     */
    APPOINTMENT(4, "预约挂号"),

    /**
     * 未知来源（兜底处理，防止解析异常）
     */
    UNKNOWN(0, "未知来源");

    /**
     * 来源编码
     */
    private final int code;

    /**
     * 来源描述
     */
    private final String label;

    /**
     * 根据来源编码获取对应的枚举实例
     *
     * @param code 来源编码
     * @return 对应的枚举实例，若未匹配则返回 UNKNOWN
     */
    public static AppointSourceEnum fromCode(int code) {
        for (AppointSourceEnum source : values()) {
            if (source.code == code) {
                return source;
            }
        }
        return UNKNOWN;
    }
}