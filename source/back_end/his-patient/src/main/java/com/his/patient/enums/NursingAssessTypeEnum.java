package com.his.patient.enums;

import lombok.Getter;

/**
 * 护理评估单类型枚举（biz_nursing_assessment.assess_type）
 */
@Getter
public enum NursingAssessTypeEnum {

    PRESSURE_ULCER(1, "压疮评估（Braden）"),
    FALL(2, "跌倒评估（Morse）"),
    PAIN(3, "疼痛评估（NRS）"),
    VTE(4, "VTE血栓评估（Caprini）"),
    TUBE(5, "管路滑脱评估");

    private final int code;
    private final String label;

    NursingAssessTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NursingAssessTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NursingAssessTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」——
     * 脏数据应由数据治理流程修复，而非界面伪装。
     */
    public static String getText(Integer code) {
        NursingAssessTypeEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，
     * 用于业务异常消息或审计日志，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        NursingAssessTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
