package com.his.pharmacy.enums;

import lombok.Getter;

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

    public static AntibioticProblemTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AntibioticProblemTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(String code) {
        AntibioticProblemTypeEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」，保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(String code) {
        if (code == null) {
            return "未知";
        }
        AntibioticProblemTypeEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
