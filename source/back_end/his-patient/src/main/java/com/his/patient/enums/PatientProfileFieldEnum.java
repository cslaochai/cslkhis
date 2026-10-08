package com.his.patient.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 患者档案关键字段（P5.1 EMPI / P5.3 数据质量共用）。
 */
@Getter
public enum PatientProfileFieldEnum {

    PATIENT_NAME("patientName", "姓名"),
    GENDER("gender", "性别"),
    BIRTH_DATE("birthDate", "出生日期"),
    ID_CARD("idCard", "身份证号"),
    PHONE("phone", "手机号"),
    ADDRESS("address", "家庭住址"),
    NATION("nation", "民族"),
    OCCUPATION("occupation", "职业"),
    MARITAL_STATUS("maritalStatus", "婚姻状况"),
    BLOOD_TYPE("bloodType", "血型"),
    CONTACT_NAME("contactName", "联系人"),
    CONTACT_PHONE("contactPhone", "联系人电话"),
    ALLERGY_HISTORY("allergyHistory", "过敏史"),
    MEDICAL_HISTORY("medicalHistory", "既往病史"),
    MEDICAL_INSURANCE_TYPE("medicalInsuranceType", "医保类型");

    private final String code;
    private final String label;

    PatientProfileFieldEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PatientProfileFieldEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PatientProfileFieldEnum e : values()) {
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
        PatientProfileFieldEnum e = fromCode(code);
        return e != null ? e.label : "";
    }

    public static String labelOrUnknown(String code) {
        if (code == null) {
            return "未知";
        }
        PatientProfileFieldEnum e = fromCode(code);
        return e != null ? e.label : "未知(" + code + ")";
    }

    /** 全部关键字段（码→中文名），顺序即展示顺序 */
    public static Map<String, String> all() {
        Map<String, String> m = new LinkedHashMap<>();
        for (PatientProfileFieldEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }
}
