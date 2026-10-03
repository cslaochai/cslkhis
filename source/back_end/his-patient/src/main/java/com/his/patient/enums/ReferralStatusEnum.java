package com.his.patient.enums;

import lombok.Getter;

/**
 * 转诊状态枚举
 */
@Getter
public enum ReferralStatusEnum {

    PENDING(0, "待确认"),
    CONFIRMED(1, "已确认"),
    FINISHED(2, "已完成"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String label;

    ReferralStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ReferralStatusEnum fromCode(int code) {
        for (ReferralStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        ReferralStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
