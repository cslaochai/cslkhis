package com.his.patient.support;

import com.his.common.enums.SysGenderEnum;

/**
 * 性别文案（P5.1 定口径，P5.6 追加「未知」，2026-09-23 并入统一口径性别字典）
 *
 */
public final class PatientGenderText {

    private PatientGenderText() {
    }

    /** 合法码值走枚举；null 与脏码值是展示层兜底，脏值带原值暴露（如 0 报"未知(0)"而非"女"） */
    public static String of(Integer gender) {
        if (gender == null) {
            return "—";
        }
        SysGenderEnum g = SysGenderEnum.fromCode(gender);
        return g != null ? g.getLabel() : "未知(" + gender + ")";
    }
}
