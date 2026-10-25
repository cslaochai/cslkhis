package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 科室类型枚举（sys_department.dept_type，字典 his_dept_type）。
 * <p>列存<b>逗号分隔的多值字符串</b>（一个科室可同时是「门诊科室+急诊科室」），
 * 匹配一律走 {@link #contains(String, DeptTypeEnum)}，禁 equals 全串比对。
 * <p>码值与 sys_dict_data（his_dept_type）逐字对齐：前端多选由字典驱动，
 * 本枚举是后端 FIND_IN_SET 与急诊域口径判定的唯一码值出处 —— 改字典值必须同步这里。
 */
@Getter
public enum DeptTypeEnum {

    /** 门诊科室 */
    OUTPATIENT("1", "门诊科室"),
    /** 医技科室 */
    TECH("2", "医技科室"),
    /** 药房 */
    PHARMACY("3", "药房"),
    /** 住院科室 */
    INPATIENT("4", "住院科室"),
    /** 其他 */
    OTHER("5", "其他"),
    /** 急诊科室：急诊值班公开 / 值班资质校验 / 固定医师占比等急诊域口径的科室判据 */
    ER("6", "急诊科室"),
    ;

    private final String code;
    private final String label;

    DeptTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeptTypeEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (DeptTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 科室的多值 dept_type 串里是否含指定类型。
     * null/空串返回 false —— 「未维护类型」不等于「不是该类型」。
     */
    public static boolean contains(String deptType, DeptTypeEnum type) {
        if (StringUtils.isEmpty(deptType) || type == null) {
            return false;
        }
        for (String part : deptType.split(",")) {
            if (part.trim().equals(type.code)) {
                return true;
            }
        }
        return false;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        DeptTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        DeptTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
