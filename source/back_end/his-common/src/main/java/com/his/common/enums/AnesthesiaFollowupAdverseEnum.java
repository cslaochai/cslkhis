package com.his.common.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 麻醉随访并发症要点
 */
@Getter
public enum AnesthesiaFollowupAdverseEnum {

    NAUSEA_VOMIT(1, "恶心呕吐"),
    PHARYNGALGIA(2, "咽痛"),
    URINARY_RETENTION(3, "尿潴留"),
    HEADACHE(4, "头痛"),
    DIZZINESS(5, "头晕"),
    NEUROLOGIC(6, "神经症状"),
    RESPIRATORY(7, "呼吸并发症"),
    HYPOTENSION_ARRHYTHMIA(8, "低血压/心律失常"),
    OTHER(9, "其他");

    private final int code;
    private final String label;

    AnesthesiaFollowupAdverseEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static Map<Integer, String> all() {
        Map<Integer, String> m = new LinkedHashMap<>();
        for (AnesthesiaFollowupAdverseEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }

    public static AnesthesiaFollowupAdverseEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AnesthesiaFollowupAdverseEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AnesthesiaFollowupAdverseEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AnesthesiaFollowupAdverseEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AnesthesiaFollowupAdverseEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
