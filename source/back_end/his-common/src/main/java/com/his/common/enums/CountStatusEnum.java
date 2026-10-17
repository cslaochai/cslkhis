package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 器械清点单状态（0-清点中 1-三轮一致清点完成 2-存在清点差异 3-异常终止）。
 */
@Getter
public enum CountStatusEnum {

    RUNNING(0, "清点中"),
    DONE(1, "三轮一致，清点完成"),
    DIFF(2, "存在清点差异"),
    ABORT(3, "异常终止");

    private final int code;
    private final String label;

    CountStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static CountStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CountStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        CountStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CountStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CountStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
