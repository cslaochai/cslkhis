package com.his.common.enums;

import lombok.Getter;

/**
 * 病历审核状态枚举
 */
@Getter
public enum ReviewStatusEnum {

    NOT_SUBMITTED(0, "未提交"),
    PENDING(1, "待审核"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已驳回");

    private final int code;
    private final String label;

    ReviewStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ReviewStatusEnum fromCode(int code) {
        for (ReviewStatusEnum status : values()) {
            if (status.code == code) return status;
        }
        return null;
    }
}
