package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 抗菌药物切口Review问题类型枚举（字符串码 41~48）。
 */
@Getter
public enum AntibioticProblemTypeEnum {

    NO_INDICATION("41", "无预防用药指征"),
    WRONG_DRUG("42", "品种选择不合理"),
    WRONG_TIMING("43", "给药时机不合理"),
    OVER_LONG_COURSE("44", "疗程过长"),
    UNNEEDED_COMBO("45", "无指征联合用药"),
    WRONG_DOSE("46", "剂量不合理"),
    SPECIAL_USE_NO_CONSULT("47", "特殊使用级无会诊"),
    UNCLEAR_POSTOP_START("48", "术后用药起点不明");

    private final String code;
    private final String label;

    AntibioticProblemTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static AntibioticProblemTypeEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (AntibioticProblemTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        AntibioticProblemTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        AntibioticProblemTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        AntibioticProblemTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
