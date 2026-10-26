package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 听班呼叫单状态枚举（sql/297，字典 his_duty_call_status）
 */
@Getter
public enum DutyCallStatusEnum {

    /**
     * 待到岗：已呼叫，被叫人还没应答
     */
    PENDING(0, "待到岗"),
    /**
     * 已到岗：被叫人已应答到岗
     */
    ARRIVED(1, "已到岗"),
    /**
     * 已超时升级：到岗时限内没人应答，已升到上一层级
     */
    ESCALATED(2, "已超时升级"),
    /**
     * 已取消：呼叫方主动撤销
     */
    CANCELED(3, "已取消");

    private final int code;
    private final String label;

    DutyCallStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 该状态是否仍在等应答（只有待到岗才算还在等）
     */
    public static boolean isWaiting(Integer code) {
        return code != null && code == PENDING.code;
    }

    public static DutyCallStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DutyCallStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DutyCallStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DutyCallStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DutyCallStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
