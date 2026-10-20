package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static NursingAssessTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NursingAssessTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        NursingAssessTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NursingAssessTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        NursingAssessTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
