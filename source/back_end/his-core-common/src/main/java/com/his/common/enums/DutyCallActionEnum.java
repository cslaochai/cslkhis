package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 听班呼叫流水动作枚举（sql/297，字典 his_duty_call_action）
 */
@Getter
public enum DutyCallActionEnum {

    /**
     * 发起呼叫
     */
    CALL(1, "发起呼叫"),
    /**
     * 到岗应答
     */
    ARRIVE(2, "到岗应答"),
    /**
     * 超时升级
     */
    ESCALATE(3, "超时升级"),
    /**
     * 取消呼叫
     */
    CANCEL(4, "取消呼叫"),
    /**
     * 通知投递失败（呼叫单本身成立，只是站内信没送出去）
     */
    NOTIFY_FAIL(5, "通知投递失败");

    private final int code;
    private final String label;

    DutyCallActionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DutyCallActionEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DutyCallActionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        DutyCallActionEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DutyCallActionEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
