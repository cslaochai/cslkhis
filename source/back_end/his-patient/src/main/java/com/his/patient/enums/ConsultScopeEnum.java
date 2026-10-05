package com.his.patient.enums;

import lombok.Getter;

/**
 * 会诊范围枚举
 */
@Getter
public enum ConsultScopeEnum {

    IN_DEPT(1, "科内会诊"),
    CROSS_DEPT(2, "科间会诊"),
    HOSPITAL(3, "全院会诊");

    private final int code;
    private final String label;

    ConsultScopeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ConsultScopeEnum fromCode(int code) {
        for (ConsultScopeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」——
     * 脏数据应由数据治理流程修复，而非界面伪装。
     */
    public static String labelOf(Integer code) {
        ConsultScopeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }
    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，
     * 用于业务异常消息或审计日志，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        ConsultScopeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
