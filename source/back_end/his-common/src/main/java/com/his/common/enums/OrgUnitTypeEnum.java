package com.his.common.enums;

import lombok.Getter;

/**
 * 排班单元类型枚举（sql/200，字典 {@code his_org_unit_type}）
 *
 * <p><b>为什么单元要带类型</b>：排班的归属有三种粒度——科室（门诊医生、病区护士站）、
 * 病区（护理三班倒按病区排，一个科室下多个病区）、全院（总值班不属于任何科室）。
 * 只按科室这一个维度表达不了后两种：全院级没有科室可归，病区级会把病区当成科室，
 * 于是「按科室查在岗人数」捞出一堆不是科室的归属。
 *
 * <p><b>全院级的单元与科室都用 0 表达，不用 NULL</b>：收口判定要写成「科室在授权集合内
 * <b>或</b> 单元类型为全院」，科室为 NULL 时 {@code IN (...)} 永远不成立也永远不报错，
 * 受限角色会静默看不到全院班（越权看不见比多看见更难发现）。
 */
@Getter
public enum OrgUnitTypeEnum {

    /** 科室（门诊出诊、科室值班） */
    DEPT(1, "科室"),
    /** 病区（护理排班的宿主粒度，一个科室可有多个病区） */
    WARD(2, "病区"),
    /** 全院（总值班、信息/总务值班，不属于任何科室） */
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

    public static String labelOf(Integer code) {
        OrgUnitTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
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
