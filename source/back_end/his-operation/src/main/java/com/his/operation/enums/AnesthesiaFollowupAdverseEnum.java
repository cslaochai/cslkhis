package com.his.operation.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 麻醉随访并发症要点（P134.3）。
 *
 * <p>码值→文案的唯一出口（原 {@code FollowupAdverseItems} 的 {@code ALL} 映射已上移至此）。
 * 不建字典，后端本枚举与前端各自单点；未知码值一律返回空串，绝不回落成某个合法值。
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

    public static AnesthesiaFollowupAdverseEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AnesthesiaFollowupAdverseEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        if (code == null) {
            return "";
        }
        AnesthesiaFollowupAdverseEnum e = fromCode(code);
        return e != null ? e.label : "";
    }

    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        AnesthesiaFollowupAdverseEnum e = fromCode(code);
        return e != null ? e.label : "未知(" + code + ")";
    }

    public static Map<Integer, String> all() {
        Map<Integer, String> m = new LinkedHashMap<>();
        for (AnesthesiaFollowupAdverseEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }
}
