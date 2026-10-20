package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 人力需求来源枚举（1-门诊出诊派生 2-住院患者派生 3-手工调整）
 */
@Getter
public enum StaffDemandSourceEnum {

    CLINIC(1, "门诊出诊派生"),
    INPATIENT(2, "住院患者派生"),
    MANUAL(3, "手工调整");

    private final int code;
    private final String label;

    StaffDemandSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static StaffDemandSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (StaffDemandSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        StaffDemandSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        StaffDemandSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        StaffDemandSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
