package com.his.equipment.enums;

import lombok.Getter;

/**
 * 计量结果枚举（码值口径 = biz_equipment_metering.metering_result 列注释）。
 */
@Getter
public enum MeteringResultEnum {

    QUALIFIED(1, "合格"),
    UNQUALIFIED(2, "不合格");

    private final int code;
    private final String label;

    MeteringResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MeteringResultEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MeteringResultEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用：null 或脏值返回空串 */
    public static String getText(Integer code) {
        MeteringResultEnum e = fromCode(code);
        return e == null ? "" : e.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」 */
    public static String labelOrUnknown(Integer code) {
        MeteringResultEnum e = fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }
}
