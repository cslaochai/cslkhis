package com.his.medicaltech.enums;

import lombok.Getter;

@Getter
public enum ReportTypeEnum {

    INSPECTION(1, "检查报告"),
    LAB_TEST(2, "检验报告");

    private final Integer code;
    private final String desc;

    ReportTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ReportTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReportTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}