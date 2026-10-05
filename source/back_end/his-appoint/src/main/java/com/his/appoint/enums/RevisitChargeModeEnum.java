package com.his.appoint.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 复诊收费方式枚举 —— 复诊收费策略命中后对挂号费/诊查费的处理动作。
 */
@Getter
@AllArgsConstructor
public enum RevisitChargeModeEnum {

    /**
     * 1-全额收费：新的一次就诊，正常收挂号费 + 诊查费。
     */
    FULL(1, "全额收费"),

    /**
     * 2-免挂号费：只免挂号费，诊查费照收。
     */
    FREE_REGIST(2, "免挂号费"),

    /**
     * 3-免挂号费+诊查费：整单 0 元。
     */
    FREE_ALL(3, "免挂号费+诊查费"),

    /**
     * 未知类型（兜底处理，防止解析异常）
     */
    UNKNOWN(0, "未知类型");

    private final int code;

    private final String label;

    public static RevisitChargeModeEnum fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (RevisitChargeModeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return UNKNOWN;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}
