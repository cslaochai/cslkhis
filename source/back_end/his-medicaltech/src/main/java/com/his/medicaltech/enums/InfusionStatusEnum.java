package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 门诊输液单状态枚举
 */
@Getter
public enum InfusionStatusEnum {

    PENDING_TEST(1, "待皮试"),
    WAITING(2, "待输注"),
    INFUSING(3, "输液中"),
    FINISHED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String label;

    InfusionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfusionStatusEnum fromCode(int code) {
        for (InfusionStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        InfusionStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
