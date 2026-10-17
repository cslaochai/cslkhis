package com.his.common.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

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

    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static Map<String, String> all() {
        Map<String, String> m = new LinkedHashMap<>();
        for (PatientDataTableEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }

    public static PatientDataTableEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (PatientDataTableEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        PatientDataTableEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        PatientDataTableEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        PatientDataTableEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
