package com.his.common.enums;

/**
 * 费用来源单据（字典 his_fee_source_type，落在费用记账流水的来源单据类型列）。
 */
public enum FeeSourceTypeEnum {

    REGISTRATION(1, "挂号"),
    PRESCRIPTION(2, "处方"),
    EXAM_APPLY(3, "检查申请"),
    LAB_APPLY(4, "检验申请"),
    TREATMENT_APPLY(5, "治疗申请"),
    DISPENSE(6, "发药/摆药"),
    CONSUMABLE(7, "耗材使用"),
    INPATIENT_ORDER(8, "住院医嘱"),
    OPERATION(9, "手术"),
    TRANSFUSION(10, "输血"),
    MANUAL(11, "手工补记账"),
    OTHER(12, "其他");

    private final Integer code;
    private final String desc;

    FeeSourceTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FeeSourceTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FeeSourceTypeEnum item : values()) {
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
        FeeSourceTypeEnum item = fromCode(code);
        return item == null ? "未知来源" : item.desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
