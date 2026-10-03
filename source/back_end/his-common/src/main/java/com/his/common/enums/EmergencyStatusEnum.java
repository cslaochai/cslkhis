package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 急诊状态枚举
 */
@Getter
@AllArgsConstructor
public enum EmergencyStatusEnum {

    /**
     * 1-候诊
     */
    WAITING(1, "候诊"),

    /**
     * 2-诊治中
     */
    TREATING(2, "诊治中"),

    /**
     * 3-留观
     */
    OBSERVATION(3, "留观"),

    /**
     * 4-转住院
     */
    ADMITTED(4, "转住院"),

    /**
     * 5-离院
     */
    DISCHARGED(5, "离院"),

    /**
     * 6-死亡
     */
    DEAD(6, "死亡"),

    /**
     * 未知状态（兜底处理，防止解析异常）
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
    public static EmergencyStatusEnum fromCode(int code) {
        for (EmergencyStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }
}