package com.his.patient.enums;

import lombok.Getter;

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
        if (code == null) {
            return null;
        }
        for (BedTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(String code) {
        BedTypeEnum item = fromCode(code);
        return item == null ? null : item.label;
    }
}
