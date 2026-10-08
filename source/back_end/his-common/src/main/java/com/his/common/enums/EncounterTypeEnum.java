package com.his.common.enums;

/**
 * 收费域就诊类型（字典 his_encounter_type，落在费用记账流水的就诊类型列等）。
 */
public enum EncounterTypeEnum {

    OUTPATIENT(1, "门诊"),
    INPATIENT(2, "住院");

    private final Integer code;
    private final String desc;

    EncounterTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static EncounterTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EncounterTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String descOf(Integer code) {
        EncounterTypeEnum item = fromCode(code);
        return item == null ? "未知" : item.desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
