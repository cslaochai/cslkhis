package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 死亡证明上报状态枚举
 */
@Getter
public enum DeathCertReportEnum {

    NONE(1, "未上报"),
    DONE(2, "已上报"),
    FAILED(3, "上报失败");

    private final int code;
    private final String label;

    DeathCertReportEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeathCertReportEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DeathCertReportEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DeathCertReportEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DeathCertReportEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DeathCertReportEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
