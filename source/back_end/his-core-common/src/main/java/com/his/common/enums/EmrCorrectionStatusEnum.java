package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病历更正申请状态
 */
@Getter
public enum EmrCorrectionStatusEnum {

    DEPT_PENDING(1, "待科室审核"),
    MEDICAL_PENDING(2, "待医务审批"),
    APPROVED(3, "已批准待修改"),
    REJECTED(4, "已驳回"),
    COMPLETED(5, "已完成"),
    REVOKED(6, "已撤回");

    private final int code;
    private final String label;

    EmrCorrectionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EmrCorrectionStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EmrCorrectionStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        EmrCorrectionStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        EmrCorrectionStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        EmrCorrectionStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
