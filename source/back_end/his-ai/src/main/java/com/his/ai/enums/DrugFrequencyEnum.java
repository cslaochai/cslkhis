package com.his.ai.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用药频次白话化词典（qd→每天 1 次 …）。
 *
 * <p>码值→文案的唯一出口（原 {@code PatientMedicationGuideCapabilityImpl} 的 {@code FREQUENCY_DICT}
 * 映射已上移至此）。只做同义改写与汉字数字转阿拉伯数字，查不到就原样返回，绝不猜测频次。
 */
@Getter
public enum DrugFrequencyEnum {

    QD("qd", "每天 1 次"),
    BID("bid", "每天 2 次"),
    TID("tid", "每天 3 次"),
    QID("qid", "每天 4 次"),
    QN("qn", "每晚 1 次"),
    HS("hs", "睡前 1 次"),
    QOD("qod", "隔天 1 次"),
    BIW("biw", "每周 2 次"),
    Q4H("q4h", "每 4 小时 1 次"),
    Q6H("q6h", "每 6 小时 1 次"),
    Q8H("q8h", "每 8 小时 1 次"),
    Q12H("q12h", "每 12 小时 1 次"),
    PRN("prn", "不舒服时按需使用"),
    ST("st", "立即使用 1 次");

    private final String code;
    private final String label;

    DrugFrequencyEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 频次缩写大小写不敏感（库里实测混着 qd / QD）。
     */
    public static DrugFrequencyEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        String key = code.trim().toLowerCase();
        for (DrugFrequencyEnum e : values()) {
            if (e.code.equals(key)) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static String getText(String code) {
        DrugFrequencyEnum e = fromCode(code);
        return e != null ? e.label : null;
    }

    public static Map<String, String> all() {
        Map<String, String> m = new LinkedHashMap<>();
        for (DrugFrequencyEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }
}
