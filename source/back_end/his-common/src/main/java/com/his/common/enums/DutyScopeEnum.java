package com.his.common.enums;

import lombok.Getter;

/**
 * 值守责任范围枚举（sql/200，字典 {@code his_duty_scope}）
 *
 * <p>「总值班」不是一个岗，是一组位：行政总值班管全院协调，急诊总值班管抢救调配，
 * 感染/总务/信息各自有自己的夜间责任人。分范围是为了<b>派单能找到对的那一位</b>——
 * 网络断了打给行政总值班，他既没权限也没口令。
 */
@Getter
public enum DutyScopeEnum {

    /** 全院行政总值班 */
    ADMIN(1, "全院行政"),
    /** 急诊总值班 */
    EMERGENCY(2, "急诊"),
    /** 医院感染总值班 */
    INFECTION(3, "感染"),
    /** 总务/后勤总值班 */
    LOGISTICS(4, "总务"),
    /** 信息科总值班 */
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

    public static DutyScopeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DutyScopeEnum scope : values()) {
            if (scope.code == code) {
                return scope;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        DutyScopeEnum scope = fromCode(code);
        return scope == null ? "未知(" + code + ")" : scope.getLabel();
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (DutyScopeEnum scope : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(scope.code).append("-").append(scope.label);
        }
        return sb.toString();
    }
}
