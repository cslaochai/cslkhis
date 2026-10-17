package com.his.common.enums;

import lombok.Getter;

/**
 * 药品追溯码码制枚举（1-GS1 2-中国药品追溯码20位 3-其他/未识别）。
 */
@Getter
public enum DrugTraceCodeTypeEnum {

    GS1(1, "GS1"),
    CN20(2, "中国药品追溯码20位"),
    OTHER(3, "其他/未识别");

    private final int code;
    private final String label;

    DrugTraceCodeTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DrugTraceCodeTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DrugTraceCodeTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(Integer code) {
        DrugTraceCodeTypeEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        DrugTraceCodeTypeEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
