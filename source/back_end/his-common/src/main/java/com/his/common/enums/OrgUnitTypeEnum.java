package com.his.common.enums;

import lombok.Getter;

/**
 * 排班单元类型枚举（sql/200，字典 his_org_unit_type）
 */
@Getter
public enum OrgUnitTypeEnum {

    /**
     * 科室（门诊出诊、科室值班）
     */
    DEPT(1, "科室"),
    /**
     * 病区（护理排班的宿主粒度，一个科室可有多个病区）
     */
    WARD(2, "病区"),
    /**
     * 全院（总值班、信息/总务值班，不属于任何科室）
     */
    HOSPITAL(3, "全院");

    private final int code;
    private final String label;

    OrgUnitTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OrgUnitTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrgUnitTypeEnum type : values()) {
            if (type.code == code) {
                return type;
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
        OrgUnitTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        OrgUnitTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 该单元类型是否要求科室ID有值：只有全院级不要求（其余两级的科室列必须能定位到真实科室）。
     */
    public static boolean requiresDept(Integer code) {
        return code == null || code != HOSPITAL.code;
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (OrgUnitTypeEnum type : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(type.code).append("-").append(type.label);
        }
        return sb.toString();
    }
}
