package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 患者主索引状态枚举
 */
@Getter
public enum PatientMergeStatusEnum {

    NORMAL(0, "正常"),
    MERGED(1, "已并入主档");

    private final int code;
    private final String label;

    PatientMergeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PatientMergeStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PatientMergeStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PatientMergeStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PatientMergeStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PatientMergeStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
