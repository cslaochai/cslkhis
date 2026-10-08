package com.his.patient.support;

import com.his.patient.enums.PatientDataTableEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * EMPI 统计业务数据量时涉及的源表（标识 → 中文名）
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PatientDataTables {

    public static final Map<String, String> LABELS = build();

    private static Map<String, String> build() {
        return Collections.unmodifiableMap(PatientDataTableEnum.all());
    }

    /**
     * 源表中文名（<b>展示用</b>）。未知标识返回空串，不回落成"其他" ——
     * 静默回落会让新加的表永远显示不出问题。
     */
    public static String label(String key) {
        return LABELS.getOrDefault(key, "");
    }

    /**
     * 源表中文名（<b>异常 / 审计用</b>）：未知标识原样报出「未知(key)」，便于排查。
     * 绝不用于前端展示。
     */
    public static String labelOrUnknown(String key) {
        return LABELS.getOrDefault(key, "未知(" + key + ")");
    }
}
