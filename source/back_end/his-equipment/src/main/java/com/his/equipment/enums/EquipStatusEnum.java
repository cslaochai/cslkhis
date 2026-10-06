package com.his.equipment.enums;

import lombok.Getter;

/**
 * 设备状态枚举（码值口径 = sys_equipment.status 列注释）。
 */
@Getter
public enum EquipStatusEnum {

    IN_USE(1, "在用"),
    DISABLED(2, "停用"),
    UNDER_REPAIR(3, "维修中"),
    SCRAPPED(4, "报废");

    private final int code;
    private final String label;

    EquipStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EquipStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EquipStatusEnum e : values()) {
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
        EquipStatusEnum e = fromCode(code);
        return e == null ? "" : e.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」 */
    public static String labelOrUnknown(Integer code) {
        EquipStatusEnum e = fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }
}
