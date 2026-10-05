package com.his.patient.enums;

import lombok.Getter;

/**
 * 离院方式枚举
 */
@Getter
public enum DischargeWayEnum {

    MEDICAL_ORDER(1, "医嘱离院"),
    TRANSFER_HOSPITAL(2, "医嘱转院"),
    TRANSFER_COMMUNITY(3, "医嘱转社区"),
    UNMEDICAL(4, "非医嘱离院"),
    DEATH(5, "死亡"),
    OTHER(9, "其他");

    private final int code;
    private final String label;

    DischargeWayEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DischargeWayEnum fromCode(int code) {
        for (DischargeWayEnum item : values()) {
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
        DischargeWayEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }
    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，
     * 用于业务异常消息或审计日志，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        DischargeWayEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
