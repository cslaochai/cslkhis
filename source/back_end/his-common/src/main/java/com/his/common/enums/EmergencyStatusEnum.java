package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 急诊状态枚举 —— 急诊就诊状态的唯一权威码值（1-6）。
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
     * 码值是否合法（写入侧校验用；null 与解析兜底档 UNKNOWN 都不合法）
     */
    public static boolean isValid(Integer code) {
        EmergencyStatusEnum item = fromCode(code);
        return item != UNKNOWN;
    }

    public static EmergencyStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EmergencyStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        EmergencyStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        EmergencyStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        EmergencyStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
