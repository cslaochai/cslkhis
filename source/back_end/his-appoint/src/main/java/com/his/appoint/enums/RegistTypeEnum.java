package com.his.appoint.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 挂号类型枚举
 */
@Getter
@AllArgsConstructor
public enum RegistTypeEnum {

    /**
     * 1-普通号
     */
    NORMAL(1, "普通号"),

    /**
     * 2-专家号
     */
    EXPERT(2, "专家号"),

    /**
     * 3-急诊号
     */
    EMERGENCY(3, "急诊号"),

    /**
     * 4-免费号
     */
    FREE(4, "免费号"),

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
    public static RegistTypeEnum fromCode(int code) {
        for (RegistTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
}