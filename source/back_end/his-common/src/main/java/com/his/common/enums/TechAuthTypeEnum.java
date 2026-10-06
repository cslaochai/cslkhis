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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        TechAuthTypeEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        TechAuthTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
