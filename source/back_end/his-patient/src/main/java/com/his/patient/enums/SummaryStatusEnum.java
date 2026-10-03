package com.his.patient.enums;

import lombok.Getter;

/**
 * 病案首页状态枚举
 */
@Getter
public enum SummaryStatusEnum {

    DRAFT(1, "草稿"),
    SUBMITTED(2, "已提交"),
    ARCHIVED(3, "已归档");

    private final int code;
    private final String label;

    SummaryStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SummaryStatusEnum fromCode(int code) {
        for (SummaryStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        SummaryStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
