package com.his.common.enums;

import lombok.Getter;

/**
 * 班内角色枚举（值守点位与值班排班共用，字典 {@code his_duty_role}）
 *
 * <p>一个点位一天一位一人，但同一班次下要分主副：<b>主班是第一个责任人，副班是顶上的人</b>
 * （主班查无、电话催不动、或被抽调时由副班承接）。
 * 只有「主/副」两档，因为再细分就需要额外的顺序语义，而升级链路只用到「找不着主班就叫副班」。
 */
@Getter
public enum DutyRoleTypeEnum {

    /** 主班：该点位当天的第一责任人 */
    PRIMARY(1, "主班"),
    /** 副班：主班不可用时顶上 */
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

    public static String labelOf(Integer code) {
        DutyRoleTypeEnum role = fromCode(code);
        return role == null ? "未知(" + code + ")" : role.getLabel();
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (DutyRoleTypeEnum role : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(role.code).append("-").append(role.label);
        }
        return sb.toString();
    }
}
