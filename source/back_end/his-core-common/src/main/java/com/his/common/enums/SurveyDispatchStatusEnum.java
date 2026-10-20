package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 满意度发放回收状态枚举
 */
@Getter
public enum SurveyDispatchStatusEnum {

    PENDING_PUSH(1, "待推送"),
    PUSHED(2, "已推送待回收"),
    RECYCLED(3, "已回收"),
    EXPIRED(4, "已过期"),
    REFUSED(5, "已拒答");

    private final int code;
    private final String label;

    SurveyDispatchStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SurveyDispatchStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SurveyDispatchStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        SurveyDispatchStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SurveyDispatchStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        SurveyDispatchStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
