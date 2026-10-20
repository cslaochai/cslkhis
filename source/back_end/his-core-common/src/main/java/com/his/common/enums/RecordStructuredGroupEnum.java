package com.his.common.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 住院病历结构化要素分组（病史 / 生命体征 / 体格检查 / 诊疗过程与结论 / 会诊 / 转科 / 手术 / 输血）。
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

    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    /** 全部分组（码→文案），供前端渲染要素字典 */
    public static Map<String, String> all() {
        Map<String, String> m = new LinkedHashMap<>();
        for (RecordStructuredGroupEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }

    public static RecordStructuredGroupEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (RecordStructuredGroupEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        RecordStructuredGroupEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        RecordStructuredGroupEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        RecordStructuredGroupEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
