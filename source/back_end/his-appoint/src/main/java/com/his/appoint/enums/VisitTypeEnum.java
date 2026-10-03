package com.his.appoint.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 就诊类型枚举
 */
@Getter
@AllArgsConstructor
public enum VisitTypeEnum {

    /**
     * 1-初诊
     */
    FIRST_VISIT(1, "初诊"),

    /**
     * 2-复诊
     */
    REVISIT(2, "复诊"),

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
    public static VisitTypeEnum fromCode(int code) {
        for (VisitTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
}