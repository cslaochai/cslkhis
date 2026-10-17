package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 执行/维保结果状态枚举（0-异常 1-正常），全仓「正常 / 异常」这一档结果判定的唯一口径
 */
@Getter
public enum TreatmentRecordStatusEnum {

    ABNORMAL(0, "异常"),
    NORMAL(1, "正常");

    private final int code;
    private final String label;

    TreatmentRecordStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static TreatmentRecordStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TreatmentRecordStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TreatmentRecordStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TreatmentRecordStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TreatmentRecordStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
