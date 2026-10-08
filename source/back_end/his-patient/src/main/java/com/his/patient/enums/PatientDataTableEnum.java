package com.his.patient.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * EMPI 统计业务数据量涉及的源表（标识 → 中文名）。
 */
@Getter
public enum PatientDataTableEnum {

    REGIST("regist", "挂号"),
    VISIT("visit", "就诊次"),
    OUTPATIENT_RECORD("outpatientRecord", "门诊病历"),
    PRESCRIPTION("prescription", "处方"),
    CHARGE("charge", "收费"),
    ADMISSION("admission", "住院"),
    INSPECTION("inspection", "检查申请"),
    LABORATORY("laboratory", "检验申请"),
    TREATMENT("treatment", "治疗申请"),
    INPATIENT_RECORD("inpatientRecord", "住院病历");

    private final String code;
    private final String label;

    PatientDataTableEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PatientDataTableEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PatientDataTableEnum e : values()) {
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
        PatientDataTableEnum e = fromCode(code);
        return e != null ? e.label : "";
    }

    /**
     * 码值→异常 / 审计文案：未知标识原样报出「未知(key)」，便于排查（绝不用于前端展示）。
     */
    public static String labelOrUnknown(String code) {
        if (code == null) {
            return "未知";
        }
        PatientDataTableEnum e = fromCode(code);
        return e != null ? e.label : "未知(" + code + ")";
    }

    public static Map<String, String> all() {
        Map<String, String> m = new LinkedHashMap<>();
        for (PatientDataTableEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }
}
