package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 床位类型枚举（normal-普通 ICU-重症 VIP-特需）
 */
@Getter
public enum BedTypeEnum {

    NORMAL("normal", "普通"),
    ICU("ICU", "重症"),
    VIP("VIP", "特需");

    private final String code;
    private final String label;

    BedTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedTypeEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (BedTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(String code) {
        BedTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        BedTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
