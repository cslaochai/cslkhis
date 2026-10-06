package com.his.patient.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 住院病历结构化要素分组（病史 / 生命体征 / 体格检查 / 诊疗过程与结论 / 会诊 / 转科 / 手术 / 输血）。
 *
 * <p>码值→文案的唯一出口（原 {@code RecordStructuredFields} 的 {@code GROUP_LABELS} 映射已上移至此）。
 */
@Getter
public enum RecordStructuredGroupEnum {

    HISTORY("history", "病史要素"),
    VITAL("vital", "生命体征"),
    EXAM("exam", "体格检查"),
    CONCLUSION("conclusion", "诊疗过程与结论"),
    CONSULT("consult", "会诊要素"),
    TRANSFER("transfer", "转科要素"),
    OPERATION("operation", "手术要素"),
    TRANSFUSION("transfusion", "输血要素");

    private final String code;
    private final String label;

    RecordStructuredGroupEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordStructuredGroupEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (RecordStructuredGroupEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    public static String getText(String code) {
        if (code == null) {
            return "";
        }
        RecordStructuredGroupEnum e = fromCode(code);
        return e != null ? e.label : "";
    }

    public static String labelOrUnknown(String code) {
        if (code == null) {
            return "未知";
        }
        RecordStructuredGroupEnum e = fromCode(code);
        return e != null ? e.label : "未知(" + code + ")";
    }

    /** 全部分组（码→文案），供前端渲染要素字典 */
    public static Map<String, String> all() {
        Map<String, String> m = new LinkedHashMap<>();
        for (RecordStructuredGroupEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }
}
