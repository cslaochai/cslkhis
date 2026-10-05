package com.his.emr.enums;

import lombok.Getter;

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

    public static SurveyDispatchStatusEnum fromCode(int code) {
        for (SurveyDispatchStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        SurveyDispatchStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
