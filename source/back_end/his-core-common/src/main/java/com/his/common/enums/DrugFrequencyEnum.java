package com.his.common.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 用药频次白话化词典（qd→每天 1 次 …）。
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

    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static Map<String, String> all() {
        Map<String, String> m = new LinkedHashMap<>();
        for (DrugFrequencyEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }

    public static DrugFrequencyEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (DrugFrequencyEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(String code) {
        DrugFrequencyEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        DrugFrequencyEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
