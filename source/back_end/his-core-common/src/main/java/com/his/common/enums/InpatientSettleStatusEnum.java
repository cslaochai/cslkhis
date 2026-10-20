package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 住院结算结果
 */
@Getter
public enum InpatientSettleStatusEnum {

    CLEARED(1, "已结清"),
    ARREARS(2, "欠费"),
    VOIDED(3, "已作废");

    private final Integer code;
    private final String label;

    InpatientSettleStatusEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static InpatientSettleStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InpatientSettleStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InpatientSettleStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InpatientSettleStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        InpatientSettleStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
