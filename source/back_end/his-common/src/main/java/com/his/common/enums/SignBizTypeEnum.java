package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 签名对象类型。
 */
public enum SignBizTypeEnum {

    /**
     * 住院病历文书
     */
    INPATIENT_RECORD(1, "住院病历"),

    /**
     * 门诊病历
     */
    OUTPATIENT_RECORD(2, "门诊病历"),

    /**
     * 住院医嘱（住院医嘱主表，医生开立 + 护士校对双签）
     */
    INPATIENT_ORDER(3, "住院医嘱"),

    /**
     * 处方（处方主表，开方医师 + 审方药师双签）
     */
    PRESCRIPTION(4, "处方"),

    /**
     * 检查报告（检查记录，报告医师 + 审核医师双签）
     */
    INSPECTION_REPORT(5, "检查报告"),

    /**
     * 检验报告（检验记录，报告医师 + 审核医师双签）
     */
    LAB_REPORT(6, "检验报告"),

    /**
     * 检查申请单（检查申请单，开单医师签名）
     */
    INSPECTION_APPLY(7, "检查申请单"),

    /**
     * 检验申请单（检验申请单，开单医师签名）
     */
    LAB_APPLY(8, "检验申请单"),

    /**
     * 病危/病重通知与告知书签收回执（病危重通知回执，告知医师签发即签，sql/161）
     */
    CRITICAL_NOTICE(9, "病危重通知"),

    /**
     * 住院请假/离院登记（住院请假登记，医师审批即签，sql/162）
     */
    INPATIENT_LEAVE(10, "住院请假单");

    private final int code;
    private final String label;

    SignBizTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static SignBizTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SignBizTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        SignBizTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SignBizTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
