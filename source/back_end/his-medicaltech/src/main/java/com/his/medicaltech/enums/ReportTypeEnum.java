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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 展示用：null 或不在枚举内返回空串（不把「未知」渲染给用户看）
     */
    public static String getText(Integer code) {
        ReportTypeEnum item = getByCode(code);
        return item == null ? "" : item.getDesc();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        ReportTypeEnum item = getByCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.getDesc();
    }
}