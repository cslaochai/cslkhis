package com.his.common.enums;

import com.his.common.exception.BusinessException;
import lombok.Getter;

/**
 * 岗位类别枚举（sql/195）
 *
 * <p><b>排班对象从「医生」泛化到「全院岗位」</b>：护士、技师、药师、收费员、导诊这些岗位每天谁在岗
 * 同样是排班员要排的东西（三班倒、节假日轮值、窗口人力），原来排班信息只有医生ID，
 * 没有岗位维度，这些岗位在 HIS 里就没有排班载体。
 *
 * <p><b>口径：人事岗位类别，不是权限角色。</b>角色表上的岗位类别字段是权威落点（角色 → 岗位类别），
 * 「人在哪个科室是什么岗位」由员工岗位（人 × 科室 × 角色）派生。
 * 排班表只存<b>类别</b>不存具体角色：排班关心的是「这个班要几个护士、几个收费员」，
 * 不是「要护士长还是分诊护士」——具体到人的岗位看员工档案。
 *
 * <p><b>号源分水岭在 {@link #DOCTOR}</b>（真实 HIS：只有出诊医生放号）：
 * <ul>
 *   <li>医生：有号源/诊室/挂号费/预约池，可加号、可停诊退号、进挂号下拉；</li>
 *   <li>其余岗位：纯出勤（谁哪天哪个班次在岗），号源恒 0、不生成时间片段、不要求诊室、不进挂号下拉。</li>
 * </ul>
 * 判定一律走 {@link #hasSource()}，不要在调用点写 {@code staffType == 1} 这种裸比较。
 *
 * <p>码值改动必须同步 {@code sql/195} 的角色表铺底段与前端 {@code lib/scheduleShift.js}。
 */
@Getter
public enum StaffTypeEnum {

    /** 医生（含急诊/放射诊断/公卫医师）：唯一有号源的类别 */
    DOCTOR(1, "医生", true),
    /** 护理（护士、分诊护士、护士长） */
    NURSE(2, "护理", false),
    /** 医技（检验技师、检查技师、营养师等） */
    MEDICAL_TECH(3, "医技", false),
    /** 药学（药剂师、临床药师） */
    PHARMACY(4, "药学", false),
    /** 收费/财务（收费员、医保结算员） */
    CASHIER(5, "收费", false),
    /** 行政/其他（管理员、导诊、病案、审计、院领导、患者） */
    ADMIN(6, "行政其他", false);

    private final int code;
    private final String label;
    /** 该岗位的排班是否承载号源（决定诊室/挂号费/时间片段/加号/停诊退号是否适用） */
    private final boolean source;

    StaffTypeEnum(int code, String label, boolean source) {
        this.code = code;
        this.label = label;
        this.source = source;
    }

    public static StaffTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StaffTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        StaffTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
    }

    /**
     * 该岗位类别的排班是否承载号源。null 一律按「无号源」处理：
     * 宁可少一个号源池，也不能让一条岗位不明的排班进挂号下拉。
     */
    public static boolean hasSource(Integer code) {
        StaffTypeEnum type = fromCode(code);
        return type != null && type.isSource();
    }

    public static boolean isDoctor(Integer code) {
        return DOCTOR.code == (code == null ? -1 : code);
    }

    /**
     * 排班写入口的合法性：岗位类别必填且在枚举内。
     * 历史数据（sql/195 之前）全部是 1-医生，由 DDL 默认值补齐，所以这里只拦新增/修改。
     *
     * <p>抛 {@code BusinessException} 而不是 IllegalArgumentException：前者是「用户选错了」，
     * 后者会被全局异常处理兜成 500，用户看到的是系统错误而不是一句人话。
     */
    public static void assertValid(Integer code) {
        if (fromCode(code) == null) {
            throw new BusinessException("岗位类别不合法：" + code + "（合法值 " + whitelistText() + "）");
        }
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (StaffTypeEnum type : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(type.code).append("-").append(type.label);
        }
        return sb.toString();
    }
}
