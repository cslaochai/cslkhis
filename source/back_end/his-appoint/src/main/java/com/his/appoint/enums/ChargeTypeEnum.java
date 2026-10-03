package com.his.appoint.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 收费类型枚举
 */
@Getter
@AllArgsConstructor
public enum ChargeTypeEnum {

    /**
     * 1-挂号费
     */
    REGISTRATION_FEE(1, "挂号费"),

    /**
     * 2-药品费
     */
    MEDICINE_FEE(2, "药品费"),

    /**
     * 3-检查费
     */
    EXAMINATION_FEE(3, "检查费"),

    /**
     * 4-检验费
     */
    LABORATORY_FEE(4, "检验费"),

    /**
     * 5-治疗费
     */
    TREATMENT_FEE(5, "治疗费"),

    /**
     * 6-综合收费
     */
    COMPREHENSIVE_FEE(6, "综合收费"),

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
    public static ChargeTypeEnum fromCode(int code) {
        for (ChargeTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
}