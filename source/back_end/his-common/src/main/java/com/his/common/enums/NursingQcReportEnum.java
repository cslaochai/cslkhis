package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 护理质量台账上报状态枚举（sql/168，落在护理质控指标台账的上报状态列）
 */
@Getter
public enum NursingQcReportEnum {

    /**
     * 未上报：可被重算覆盖
     */
    UNREPORTED(1, "未上报"),
    /**
     * 已上报：重算跳过，只能先退回
     */
    REPORTED(2, "已上报");

    private final int code;
    private final String label;

    NursingQcReportEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static NursingQcReportEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NursingQcReportEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        NursingQcReportEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NursingQcReportEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        NursingQcReportEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
