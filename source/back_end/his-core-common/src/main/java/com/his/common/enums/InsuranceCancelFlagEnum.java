package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 退费是否已撤销医保报盘（码值口径见该列注释）。
 */
@Getter
public enum InsuranceCancelFlagEnum {

    NOT_INVOLVED(0, "不涉及"),
    CANCEL_SENT(1, "已发撤销报盘");

    private final Integer code;
    private final String desc;

    InsuranceCancelFlagEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static InsuranceCancelFlagEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InsuranceCancelFlagEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InsuranceCancelFlagEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InsuranceCancelFlagEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        InsuranceCancelFlagEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}
