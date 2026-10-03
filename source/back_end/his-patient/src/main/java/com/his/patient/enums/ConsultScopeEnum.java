package com.his.patient.enums;

import lombok.Getter;

/**
 * 会诊范围枚举
 */
@Getter
public enum ConsultScopeEnum {

    IN_DEPT(1, "科内会诊"),
    CROSS_DEPT(2, "科间会诊"),
    HOSPITAL(3, "全院会诊");

    private final int code;
    private final String label;

    ConsultScopeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ConsultScopeEnum fromCode(int code) {
        for (ConsultScopeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        ConsultScopeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
