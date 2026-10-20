package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum ReportTypeEnum {

    INSPECTION(1, "检查报告"),
    LAB_TEST(2, "检验报告");

    private final Integer code;
    private final String desc;

    ReportTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static ReportTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ReportTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ReportTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ReportTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        ReportTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}