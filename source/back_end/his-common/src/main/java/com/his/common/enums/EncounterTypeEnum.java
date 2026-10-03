package com.his.common.enums;

/**
 * 收费域就诊类型（字典 {@code his_encounter_type}，落在费用记账流水的就诊类型列等）。
 *
 * <p>与 {@code VisitTypeEnum}（初诊/复诊）、就诊次表不是一回事，
 * 三者同叫 visit 会漂移，所以收费四层统一用 encounter 这个词表达"这次费用挂在谁身上"。
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

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
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

    public static String descOf(Integer code) {
        EncounterTypeEnum item = fromCode(code);
        return item == null ? "未知" : item.desc;
    }
}
