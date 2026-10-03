package com.his.patient.enums;

import lombok.Getter;

/**
 * 医嘱模板/组套共享范围枚举
 */
@Getter
public enum TemplateScopeEnum {

    PERSONAL(1, "个人"),
    DEPT(2, "科室"),
    HOSPITAL(3, "全院");

    private final int code;
    private final String label;

    TemplateScopeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TemplateScopeEnum fromCode(int code) {
        for (TemplateScopeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        TemplateScopeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
