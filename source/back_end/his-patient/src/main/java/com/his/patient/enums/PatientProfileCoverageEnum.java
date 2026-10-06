package com.his.patient.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 患者档案分组覆盖键（同时有两份存储的字段：过敏史 / 既往病史 / 联系人）。
 *
 * <p>码值→中文名的唯一出口（原 {@code PatientProfileFields} 的 {@code PROFILE_KEY_TO_LABEL} 映射已上移至此）。
 * 用于以结构化表的实际数据修正完整度缺失判据。
 */
@Getter
public enum PatientProfileCoverageEnum {

    ALLERGY("allergy", "过敏史"),
    PAST_DISEASE("pastDisease", "既往病史"),
    CONTACT("contact", "联系人");

    private final String code;
    private final String label;

    PatientProfileCoverageEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PatientProfileCoverageEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PatientProfileCoverageEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static String getText(String code) {
        if (code == null) {
            return "";
        }
        PatientProfileCoverageEnum e = fromCode(code);
        return e != null ? e.label : "";
    }

    public static String labelOrUnknown(String code) {
        if (code == null) {
            return "未知";
        }
        PatientProfileCoverageEnum e = fromCode(code);
        return e != null ? e.label : "未知(" + code + ")";
    }

    public static Map<String, String> all() {
        Map<String, String> m = new LinkedHashMap<>();
        for (PatientProfileCoverageEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }
}
