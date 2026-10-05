package com.his.patient.enums;

import lombok.Getter;

/**
 * 收治指令状态枚举
 */
@Getter
public enum AdmissionOrderStatusEnum {

    PENDING(1, "待收治"),
    ADMITTED(2, "已收治"),
    VOIDED(3, "已作废"),
    EXPIRED(4, "已过期");

    private final int code;
    private final String label;

    AdmissionOrderStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdmissionOrderStatusEnum fromCode(int code) {
        for (AdmissionOrderStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」——
     * 脏数据应由数据治理流程修复，而非界面伪装。
     */
    public static String labelOf(Integer code) {
        AdmissionOrderStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }
    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，
     * 用于业务异常消息或审计日志，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        AdmissionOrderStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
