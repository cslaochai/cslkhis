package com.his.emr.enums;

import com.his.emr.support.QcSnapshot;

/**
 * 规则的适用范围。
 */
public enum QcScopeEnum {

    /**
     * 仅门诊病历
     */
    OUTPATIENT("仅门诊病历"),

    /**
     * 门诊病历 + 住院入院记录（record_type=1）。
     * 这两者是文书家族里唯一承载"主诉/现病史/既往史/过敏史/诊疗计划"结构化要素的载体。
     */
    OUTPATIENT_AND_ENTRY("门诊病历与住院入院记录"),

    /**
     * 仅住院文书（全部类型）
     */
    INPATIENT("仅住院文书"),

    /**
     * 仅住院非入院记录文书（病程记录 / 手术记录 / 会诊记录 / 转科记录 / 输血记录…）。
     * 这类文书没有结构化要素，靠"正文"承载内容，所以查的是正文而不是主诉。
     */
    INPATIENT_NOTE("仅住院记录类文书"),

    /**
     * 不区分来源与类型：书写医生、性别与诊断矛盾、生命体征越界这类与文书类型无关的检查。
     */
    ALL("全部门诊与住院文书");

    private final String text;

    QcScopeEnum(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
