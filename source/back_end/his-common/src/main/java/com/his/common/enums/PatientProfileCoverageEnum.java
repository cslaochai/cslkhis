package com.his.common.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 患者档案分组覆盖键（同时有两份存储的字段：过敏史 / 既往病史 / 联系人）。
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

    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static Map<String, String> all() {
        Map<String, String> m = new LinkedHashMap<>();
        for (PatientProfileCoverageEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }

    public static PatientProfileCoverageEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (PatientProfileCoverageEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        PatientProfileCoverageEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        PatientProfileCoverageEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        PatientProfileCoverageEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
