package com.his.common.enums;

import lombok.Getter;

/**
 * 班内角色枚举（值守点位与值班排班共用，字典 his_duty_role）
 */
@Getter
public enum DutyRoleTypeEnum {

    /**
     * 主班：该点位当天的第一责任人
     */
    PRIMARY(1, "主班"),
    /**
     * 副班：主班不可用时顶上
     */
    SECONDARY(2, "副班");

    private final int code;
    private final String label;

    DutyRoleTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DutyRoleTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DutyRoleTypeEnum role : values()) {
            if (role.code == code) {
                return role;
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
        DutyRoleTypeEnum role = fromCode(code);
        return role == null ? "未知(" + code + ")" : role.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        DutyRoleTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (DutyRoleTypeEnum role : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(role.code).append("-").append(role.label);
        }
        return sb.toString();
    }
}
