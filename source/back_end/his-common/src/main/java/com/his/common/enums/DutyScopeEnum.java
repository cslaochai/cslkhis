package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 值守责任范围枚举（sql/200，字典 his_duty_scope）
 */
@Getter
public enum DutyScopeEnum {

    /**
     * 全院行政总值班
     */
    ADMIN(1, "全院行政"),
    /**
     * 急诊总值班
     */
    EMERGENCY(2, "急诊"),
    /**
     * 医院感染总值班
     */
    INFECTION(3, "感染"),
    /**
     * 总务/后勤总值班
     */
    LOGISTICS(4, "总务"),
    /**
     * 信息科总值班
     */
    IT(5, "信息"),
    /**
     * 临床科室值班（sql/202）：住院医师值班点位，按科室铺、带层级（一线/二线/三线）。
     *
     * <p>它跟「全院行政总值班」是两条线：行政总值班管协调（网络断了找他），
     * 临床值班管病人（一线解决不了升二线）。混在一个范围里，派单时就会叫错人。
     */
    CLINICAL(6, "临床科室");

    private final int code;
    private final String label;

    DutyScopeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static DutyScopeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DutyScopeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DutyScopeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DutyScopeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DutyScopeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
