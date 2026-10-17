package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 随访类型枚举
 */
@Getter
public enum FollowupTypeEnum {

    CHRONIC(2, "慢病随访"),
    REVISIT_REMIND(1, "复诊提醒"),
    MEDICATION_GUIDE(3, "用药指导"),
    POST_OPERATION(4, "术后随访");

    private final int code;
    private final String label;

    FollowupTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FollowupTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (FollowupTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        FollowupTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        FollowupTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        FollowupTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
