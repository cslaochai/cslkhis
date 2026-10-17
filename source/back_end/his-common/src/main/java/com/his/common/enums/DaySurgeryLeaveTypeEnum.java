package com.his.common.enums;

import lombok.Getter;

/**
 * 日间手术离院方式（1-按时离院 2-转普通住院 3-非计划再入院）
 */
@Getter
public enum DaySurgeryLeaveTypeEnum {

    NORMAL(1, "按时离院"),
    TRANSFER(2, "转普通住院"),
    READMIT(3, "非计划再入院");

    private final int code;
    private final String label;

    DaySurgeryLeaveTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DaySurgeryLeaveTypeEnum fromCode(int code) {
        for (DaySurgeryLeaveTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        DaySurgeryLeaveTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        DaySurgeryLeaveTypeEnum item = code == null ? null : fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
