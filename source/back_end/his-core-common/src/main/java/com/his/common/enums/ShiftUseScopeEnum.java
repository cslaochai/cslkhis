package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 班次适用域枚举（sql/166）
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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
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

    public static ShiftUseScopeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ShiftUseScopeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ShiftUseScopeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ShiftUseScopeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ShiftUseScopeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
