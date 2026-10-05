package com.his.patient.enums;

import lombok.Getter;

/**
 * VTE 预防措施类别枚举
 */
@Getter
public enum VteMeasureTypeEnum {

    BASIC(1, "基础预防"),
    PHYSICAL(2, "物理预防"),
    DRUG(3, "药物预防");

    private final int code;
    private final String label;

    VteMeasureTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VteMeasureTypeEnum fromCode(int code) {
        for (VteMeasureTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        VteMeasureTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
