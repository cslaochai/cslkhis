package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 药品追溯码状态枚举（1-在库 2-已发药核销 3-已作废）。
 */
@Getter
public enum DrugTraceStatusEnum {

    IN_STOCK(1, "在库"),
    DISPENSED(2, "已发药核销"),
    VOID(3, "已作废");

    private final int code;
    private final String label;

    DrugTraceStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DrugTraceStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DrugTraceStatusEnum item : values()) {
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
        DrugTraceStatusEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        DrugTraceStatusEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
