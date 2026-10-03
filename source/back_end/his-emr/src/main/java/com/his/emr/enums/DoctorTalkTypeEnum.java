package com.his.emr.enums;

import lombok.Getter;

/**
 * 处方约谈类型枚举
 */
@Getter
public enum DoctorTalkTypeEnum {

    FIRST(1, "首次约谈"),
    WARNING(2, "警告约谈"),
    LIMIT(3, "限制处方权"),
    REVOKE(4, "取消处方权"),
    RESTORE(5, "恢复处方权");

    private final int code;
    private final String label;

    DoctorTalkTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DoctorTalkTypeEnum fromCode(int code) {
        for (DoctorTalkTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DoctorTalkTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
