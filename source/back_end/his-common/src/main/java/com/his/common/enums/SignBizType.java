package com.his.common.enums;

/**
 * 签名对象类型。
 *
 * <p>与电子签名证据的业务类型列同值；每个类型对应一个
 * {@link com.his.common.service.SignableContentProvider} 实现，
 * 由实现方（业务模块）负责"内容怎么规范化"与"锚点写哪张表"。
 */
public enum SignBizType {

    /** 住院病历文书 */
    INPATIENT_RECORD(1, "住院病历"),

    /** 门诊病历 */
    OUTPATIENT_RECORD(2, "门诊病历"),

    /** 住院医嘱（住院医嘱主表，医生开立 + 护士校对双签） */
    INPATIENT_ORDER(3, "住院医嘱"),

    /** 处方（处方主表，开方医师 + 审方药师双签） */
    PRESCRIPTION(4, "处方"),

    /** 检查报告（检查记录，报告医师 + 审核医师双签） */
    INSPECTION_REPORT(5, "检查报告"),

    /** 检验报告（检验记录，报告医师 + 审核医师双签） */
    LAB_REPORT(6, "检验报告"),

    /** 检查申请单（检查申请单，开单医师签名） */
    INSPECTION_APPLY(7, "检查申请单"),

    /** 检验申请单（检验申请单，开单医师签名） */
    LAB_APPLY(8, "检验申请单"),

    /** 病危/病重通知与告知书签收回执（病危重通知回执，告知医师签发即签，sql/161） */
    CRITICAL_NOTICE(9, "病危重通知"),

    /** 住院请假/离院登记（住院请假登记，医师审批即签，sql/162） */
    INPATIENT_LEAVE(10, "住院请假单");

    private final int code;
    private final String text;

    SignBizType(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static SignBizType parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (SignBizType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        return null;
    }

    /**
     * 码值 → 文案。**未知码值原样渲染成未知(n)，不回落成某个合法值** ——
     * 回落会把"库里有个我们不认识的类型"伪装成正常数据。
     */
    public static String textOf(Integer code) {
        SignBizType t = parse(code);
        if (t != null) {
            return t.text;
        }
        return code == null ? "—" : "未知(" + code + ")";
    }
}
