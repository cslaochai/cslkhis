package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

public enum CdrInsuranceSettleStatusEnum {
    PENDING(1, "待结算"),
    SETTLED(2, "已结算"),
    UPLOADED(3, "已上传"),
    AUDITED(4, "已审核");

    private final int code;
    private final String label;

    CdrInsuranceSettleStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrInsuranceSettleStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CdrInsuranceSettleStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CdrInsuranceSettleStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CdrInsuranceSettleStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CdrInsuranceSettleStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
