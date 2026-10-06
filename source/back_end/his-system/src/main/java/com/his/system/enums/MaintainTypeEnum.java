package com.his.system.enums;

import lombok.Getter;

/**
 * 设备维保类型枚举（码值口径 = biz_equipment_maintain.maintain_type 列注释）。
 */
@Getter
public enum MaintainTypeEnum {

    MAINTENANCE(1, "保养"),
    REPAIR(2, "维修"),
    INSPECTION(3, "巡检");

    private final int code;
    private final String label;

    MaintainTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MaintainTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MaintainTypeEnum e : values()) {
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

    /** 展示用：null 或脏值返回空串（不把「未知」渲染给用户看） */
    public static String getText(Integer code) {
        MaintainTypeEnum e = fromCode(code);
        return e == null ? "" : e.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」，保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        MaintainTypeEnum e = fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }
}
