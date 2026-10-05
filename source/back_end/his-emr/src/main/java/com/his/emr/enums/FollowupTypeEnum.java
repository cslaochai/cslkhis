package com.his.emr.enums;

import lombok.Getter;

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

    public static FollowupTypeEnum fromCode(int code) {
        for (FollowupTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。
     */
    public static String labelOf(Integer code) {
        FollowupTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
