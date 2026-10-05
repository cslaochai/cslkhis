package com.his.patient.enums;

import lombok.Getter;

/**
 * 医嘱类别枚举
 */
@Getter
public enum OrderClassEnum {

    DRUG(1, "药品"),
    EXAMINATION(2, "检查"),
    LABORATORY(3, "检验"),
    TREATMENT(4, "治疗"),
    NURSING(5, "护理"),
    OPERATION(6, "手术"),
    TRANSFUSION(7, "输血"),
    MONITORING(8, "监护"),
    OTHER(9, "其他"),
    NUTRITION(10, "临床营养");

    private final int code;
    private final String label;

    OrderClassEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OrderClassEnum fromCode(int code) {
        for (OrderClassEnum item : values()) {
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
        OrderClassEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }
    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，
     * 用于业务异常消息或审计日志，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        OrderClassEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
