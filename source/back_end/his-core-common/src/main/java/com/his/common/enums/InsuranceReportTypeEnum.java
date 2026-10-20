package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 医保报盘报文类型（码值口径见建表 SQL 的列注释）。
 */
@Getter
public enum InsuranceReportTypeEnum {

    UPLOAD(1, "上报报文"),
    CANCEL(2, "撤销报文");

    private final Integer code;
    private final String desc;

    InsuranceReportTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static InsuranceReportTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InsuranceReportTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InsuranceReportTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InsuranceReportTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        InsuranceReportTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}
