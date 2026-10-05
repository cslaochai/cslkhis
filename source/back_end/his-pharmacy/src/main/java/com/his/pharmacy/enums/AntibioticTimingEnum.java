package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 抗菌药物用药时机枚举（1-术前0.5~1小时 2-术前>1小时 3-术前<0.5小时
 * 4-术中追加 5-术后才开始 6-未使用）。
 */
@Getter
public enum AntibioticTimingEnum {

    PRE_30_60_MIN(1, "术前0.5~1小时"),
    PRE_OVER_1H(2, "术前>1小时"),
    PRE_UNDER_30MIN(3, "术前<0.5小时"),
    INTRAOP_BOOST(4, "术中追加"),
    POSTOP_START(5, "术后才开始"),
    UNUSED(6, "未使用");

    private final int code;
    private final String label;

    AntibioticTimingEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AntibioticTimingEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AntibioticTimingEnum item : values()) {
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
        AntibioticTimingEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        AntibioticTimingEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
