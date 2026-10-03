package com.his.common.enums;

import lombok.Getter;

/**
 * 病历状态枚举
 */
@Getter
public enum RecordStatusEnum {

    DRAFT(1, "草稿"),
    SUBMITTED(2, "已提交"),
    ARCHIVED(3, "已归档"),
    VOIDED(4, "已作废");

    private final int code;
    private final String label;

    RecordStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordStatusEnum fromCode(int code) {
        for (RecordStatusEnum status : values()) {
            if (status.code == code) return status;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，前端渲染「未知(n)」，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        RecordStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? null : status.label;
    }
}
