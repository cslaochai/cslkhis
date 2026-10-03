package com.his.patient.enums;

import lombok.Getter;

/**
 * 给食途径枚举
 */
@Getter
public enum DietRouteEnum {

    ORAL(1, "口服"),
    TUBE(2, "管饲"),
    IV(3, "静脉（肠外）");

    private final int code;
    private final String label;

    DietRouteEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DietRouteEnum fromCode(int code) {
        for (DietRouteEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DietRouteEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
