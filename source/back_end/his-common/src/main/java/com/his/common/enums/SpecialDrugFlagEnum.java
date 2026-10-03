package com.his.common.enums;

import lombok.Getter;

/**
 * 特殊管理药品分类枚举
 */
@Getter
public enum SpecialDrugFlagEnum {

    NORMAL(0, "普通"),
    NARCOTIC(1, "麻醉药品"),
    PSYCHOTROPIC_1(2, "第一类精神药品"),
    PSYCHOTROPIC_2(3, "第二类精神药品"),
    TOXIC(4, "毒性药品");

    private final int code;
    private final String label;

    SpecialDrugFlagEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SpecialDrugFlagEnum fromCode(int code) {
        for (SpecialDrugFlagEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        SpecialDrugFlagEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
