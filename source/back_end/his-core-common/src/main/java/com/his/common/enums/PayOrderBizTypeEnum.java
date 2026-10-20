package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 支付订单业务类型
 */
@Getter
public enum PayOrderBizTypeEnum {

    OUTPATIENT_PAY(1, "门诊缴费"),
    REGISTER_FEE(2, "挂号费"),
    INPATIENT_DEPOSIT(3, "住院押金");

    private final Integer code;
    private final String label;

    PayOrderBizTypeEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static PayOrderBizTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PayOrderBizTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PayOrderBizTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PayOrderBizTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PayOrderBizTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
