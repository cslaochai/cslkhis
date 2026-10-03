package com.his.appoint.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 队列类型枚举
 */
@Getter
@AllArgsConstructor
public enum QueueTypeEnum {

    /**
     * 1-普通队列
     */
    NORMAL(1, "普通队列"),

    /**
     * 2-优先队列
     */
    PRIORITY(2, "优先队列"),

    /**
     * 3-过号队列
     */
    OVERDUE(3, "过号队列"),

    /**
     * 未知类型（兜底处理，防止解析异常）
     */
    UNKNOWN(0, "未知类型");

    /**
     * 类型编码
     */
    private final int code;

    /**
     * 类型描述
     */
    private final String label;

    /**
     * 根据类型编码获取对应的枚举实例
     *
     * @param code 类型编码
     * @return 对应的枚举实例，若未匹配则返回 UNKNOWN
     */
    public static QueueTypeEnum fromCode(int code) {
        for (QueueTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
}