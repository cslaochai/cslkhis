package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 医保报盘报文状态（码值口径见建表 SQL 的列注释）。
 */
@Getter
public enum InsuranceReportStatusEnum {

    INIT(0, "初始"),
    SUCCESS(1, "回执成功"),
    FAIL(2, "回执失败"),
    CANCELLED(3, "已撤销");

    private final Integer code;
    private final String desc;

    InsuranceReportStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static InsuranceReportStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InsuranceReportStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InsuranceReportStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InsuranceReportStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        InsuranceReportStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}
