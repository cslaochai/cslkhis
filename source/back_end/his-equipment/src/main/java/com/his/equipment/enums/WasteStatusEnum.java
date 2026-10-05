package com.his.equipment.enums;

import lombok.Getter;

/**
 * 医疗废物状态枚举（码值口径 = biz_medical_waste.status 列注释）。
 *
 * <p>文案供后端拼提示与 VO 回填用（本码值无字典表，后端即唯一文案口径）。
 */
@Getter
public enum WasteStatusEnum {

    REGISTERED(1, "已登记"),
    HANDED_OVER(2, "已交接"),
    DISPOSED(3, "已处置");

    private final int code;
    private final String label;

    WasteStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static WasteStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WasteStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        WasteStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
