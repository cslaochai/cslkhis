package com.his.operation.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 手术安全核查单时段（三方 × 三时段）。
 *
 * <p>码值→文案的唯一出口（原 {@code SafetyCheckItems} 的 {@code PHASE_LABELS} 映射已上移至此）。
 * 1-麻醉诱导前(Sign In) 2-手术开始前(Time Out) 3-患者离开手术室前(Sign Out)。
 */
@Getter
public enum OperationSafetyPhaseEnum {

    SIGN_IN(1, "麻醉诱导前（Sign In）"),
    TIME_OUT(2, "手术开始前（Time Out）"),
    SIGN_OUT(3, "患者离开手术室前（Sign Out）");

    private final int code;
    private final String label;

    OperationSafetyPhaseEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OperationSafetyPhaseEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperationSafetyPhaseEnum e : values()) {
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
        OperationSafetyPhaseEnum e = fromCode(code);
        return e != null ? e.label : "";
    }

    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        OperationSafetyPhaseEnum e = fromCode(code);
        return e != null ? e.label : "未知(" + code + ")";
    }

    /** 全部时段（码→文案），供前端渲染三张核查卡 */
    public static Map<Integer, String> all() {
        Map<Integer, String> m = new LinkedHashMap<>();
        for (OperationSafetyPhaseEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }
}
