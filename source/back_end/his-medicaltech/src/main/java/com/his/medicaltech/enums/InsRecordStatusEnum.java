package com.his.medicaltech.enums;

import lombok.Getter;

@Getter
public enum InsRecordStatusEnum {

    REGISTERED(1, "已登记"),
    SIGNED_IN(2, "已签到"),
    CHECKING(3, "检查中"),
    RESULTED(4, "已出结果"),
    REVIEWED(5, "已审核"),
    PUBLISHED(6, "已发布"),
    CANCELLED(7, "已取消");

    private final Integer code;
    private final String desc;

    InsRecordStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InsRecordStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InsRecordStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}