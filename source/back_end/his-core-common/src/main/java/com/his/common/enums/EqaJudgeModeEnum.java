package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 室间质评判定口径枚举（0-无法判定 1-SDI 2-允许总误差 3-可接受范围）
 */
@Getter
public enum EqaJudgeModeEnum {

    NONE(0, "无法判定"),
    SDI(1, "SDI"),
    TEA(2, "允许总误差"),
    RANGE(3, "可接受范围");

    private final int code;
    private final String label;

    EqaJudgeModeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static EqaJudgeModeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EqaJudgeModeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        EqaJudgeModeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        EqaJudgeModeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        EqaJudgeModeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
