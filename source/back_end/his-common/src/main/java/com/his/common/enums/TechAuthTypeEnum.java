package com.his.common.enums;

import lombok.Getter;

/**
 * 技术授权类型枚举（码值口径 = sys_employee_tech_auth.auth_type 列注释）。
 *
 * <p>同域状态枚举见 {@link TechAuthStatusEnum}（sql/155）。
 */
@Getter
public enum TechAuthTypeEnum {

    INDEPENDENT(1, "独立授权"),
    SUPERVISED(2, "上级指导下"),
    RESTRICTED(3, "限制授权");

    private final int code;
    private final String label;

    TechAuthTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TechAuthTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TechAuthTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        TechAuthTypeEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }
}
