package com.his.patient.enums;

import lombok.Getter;

/**
 * 会诊类别枚举
 */
@Getter
public enum ConsultCategoryEnum {

    NORMAL(1, "普通科间会诊"),
    NUTRITION(2, "营养会诊"),
    PHARMACY(3, "药学会诊"),
    OTHER(4, "其他专科会诊");

    private final int code;
    private final String label;

    ConsultCategoryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ConsultCategoryEnum fromCode(int code) {
        for (ConsultCategoryEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        ConsultCategoryEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
