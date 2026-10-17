package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static DutyRoleTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DutyRoleTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DutyRoleTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DutyRoleTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DutyRoleTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
