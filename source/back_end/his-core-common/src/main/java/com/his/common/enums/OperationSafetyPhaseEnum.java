package com.his.common.enums;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 手术安全核查单时段（三方 × 三时段）。
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

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 全部时段（码→文案），供前端渲染三张核查卡 */
    public static Map<Integer, String> all() {
        Map<Integer, String> m = new LinkedHashMap<>();
        for (OperationSafetyPhaseEnum e : values()) {
            m.put(e.code, e.label);
        }
        return m;
    }

    public static OperationSafetyPhaseEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (OperationSafetyPhaseEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        OperationSafetyPhaseEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        OperationSafetyPhaseEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        OperationSafetyPhaseEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
