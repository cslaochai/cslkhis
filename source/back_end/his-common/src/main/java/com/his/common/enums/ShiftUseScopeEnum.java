package com.his.common.enums;

import lombok.Getter;

/**
 * 班次适用域枚举（sql/166）
 *
 * <p>班次原先只服务门诊医生排班，「护理白班/前夜班/后夜班」一旦进了同一个下拉，
 * 门诊排班页就会选出「后夜班 00:00~08:00」这种对挂号毫无意义的班——所以按适用域分册：
 * 门诊排班写入口只认 1，护理排班只认 2，下拉默认只列自己那一册。
 * <br>字典权威在本枚举，码值改动必须同步 {@code sql/166} 的 {@code his_shift_scope} 段。
 *
 * <p><b>sql/200 起又分出两册</b>：3-全院值守给总值班/听班用（跨零点夜班只在这册合法），
 * 4-全院通用给行政/窗口/医技等「不属于门诊也不属于病区」的岗位用。
 * 分册的意义不是分类好看，而是<b>写入侧的跨零点闸门按册放行</b>：
 * 门诊册禁止跨零点（号源段与按自然日挂号表达不了一天之外），值守册必须允许。
 */
@Getter
public enum ShiftUseScopeEnum {

    /**
     * 门诊医生排班（排班信息 / 排班周模板用）
     */
    OUTPATIENT(1, "门诊排班"),
    /**
     * 病区护理排班（病区护理排班表用）
     */
    NURSING(2, "病区护理"),
    /**
     * 全院值守（总值班/科室听班用，唯一允许跨零点的册）
     */
    DUTY(3, "全院值守"),
    /**
     * 全院通用（行政、窗口、医技等非门诊非病区的班次）
     */
    GENERAL(4, "全院通用");

    private final int code;
    private final String label;

    ShiftUseScopeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ShiftUseScopeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ShiftUseScopeEnum scope : values()) {
            if (scope.code == code) {
                return scope;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        ShiftUseScopeEnum scope = fromCode(code);
        return scope == null ? "未知(" + code + ")" : scope.getLabel();
    }

    /**
     * 该册是否允许跨零点班次。只有值守册与通用册允许（班次归开始日）；
     * 门诊册的号源时段与「按自然日挂号」都表达不了一天之外，护理册按「归属当天」算工时，
     * 两处都维持 sql/63 的老铁律。缺省按最严处理。
     */
    public static boolean allowsCrossDay(Integer code) {
        return code != null && (code == DUTY.code || code == GENERAL.code);
    }

    /**
     * 该册的班次是否可被门诊排班写入口选走：门诊册与通用册可以，
     * 护理册与值守册不行（把「后夜班 00:00~08:00」排进门诊等于给患者约一个没有医生的时段）。
     */
    public static boolean usableByOutpatient(Integer code) {
        return code == null || code == OUTPATIENT.code || code == GENERAL.code;
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (ShiftUseScopeEnum scope : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(scope.code).append("-").append(scope.label);
        }
        return sb.toString();
    }
}
