package com.his.system.enums;

import lombok.Getter;

/**
 * 设备维保结果枚举（码值口径 = biz_equipment_maintain.maintain_result 列注释）。
 */
@Getter
public enum MaintainResultEnum {

    NORMAL(1, "正常"),
    ABNORMAL(2, "异常");

    private final int code;
    private final String label;

    MaintainResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MaintainResultEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MaintainResultEnum e : values()) {
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
        MaintainResultEnum e = fromCode(code);
        return e == null ? "" : e.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」 */
    public static String labelOrUnknown(Integer code) {
        MaintainResultEnum e = fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }
}
