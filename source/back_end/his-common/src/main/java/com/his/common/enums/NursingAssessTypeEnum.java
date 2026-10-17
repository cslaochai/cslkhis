package com.his.common.enums;

import lombok.Getter;

/**
 * 护理评估单类型枚举
 */
@Getter
public enum NursingAssessTypeEnum {

    PRESSURE_ULCER(1, "压疮评估（Braden）", "Braden", 6, 23),
    FALL(2, "跌倒评估（Morse）", "Morse", 0, 125),
    PAIN(3, "疼痛评估（NRS）", "NRS", 0, 10),
    VTE(4, "VTE血栓评估（Caprini）", "Caprini", 0, 58),
    TUBE(5, "管路滑脱评估", "管路滑脱", 0, 24);

    private final int code;
    private final String label;
    /**
     * 量表简称（校验提示用）
     */
    private final String scaleName;
    /**
     * 总分下限
     */
    private final int minScore;
    /**
     * 总分上限
     */
    private final int maxScore;

    NursingAssessTypeEnum(int code, String label, String scaleName, int minScore, int maxScore) {
        this.code = code;
        this.label = label;
        this.scaleName = scaleName;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    /**
     * 总分是否在量表分档区间内。
     */
    public boolean scoreInRange(int score) {
        return score >= minScore && score <= maxScore;
    }

    /**
     * 校验提示文案，如「Braden 总分应在 6~23」。
     */
    public String rangeText() {
        return scaleName + " 总分应在 " + minScore + "~" + maxScore;
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
