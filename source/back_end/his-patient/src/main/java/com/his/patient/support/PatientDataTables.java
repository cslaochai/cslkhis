package com.his.patient.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * EMPI 统计业务数据量时涉及的源表（标识 → 中文名）
 *
 * <p>标识必须与 {@code PatientIndexMapper.countDataByPatientIds} 里 UNION ALL 的
 * 第一列常量严格一致；改一处必须改另一处，否则页面会显示成"未知(regist)"。
 */
public final class PatientDataTables {

    public static final Map<String, String> LABELS = build();

    private PatientDataTables() {
    }

    private static Map<String, String> build() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("regist", "挂号");
        m.put("visit", "就诊次");
        m.put("outpatientRecord", "门诊病历");
        m.put("prescription", "处方");
        m.put("charge", "收费");
        m.put("admission", "住院");
        m.put("inspection", "检查申请");
        m.put("laboratory", "检验申请");
        m.put("treatment", "治疗申请");
        m.put("inpatientRecord", "住院病历");
        return Collections.unmodifiableMap(m);
    }

    /**
     * 未知标识原样报出，不回落成"其他" —— 静默回落会让新加的表永远显示不出问题
     */
    public static String label(String key) {
        return LABELS.getOrDefault(key, "未知(" + key + ")");
    }
}
