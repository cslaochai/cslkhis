package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 抗菌药物分级枚举（0-非抗菌药物 1-非限制使用级 2-限制使用级 3-特殊使用级）。
 */
@Getter
public enum AntibioticLevelEnum {

    NONE(0, "非抗菌药物"),
    UNRESTRICTED(1, "非限制使用级"),
    RESTRICTED(2, "限制使用级"),
    SPECIAL(3, "特殊使用级");

    private final int code;
    private final String label;

    AntibioticLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AntibioticLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AntibioticLevelEnum item : values()) {
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
        AntibioticLevelEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        AntibioticLevelEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
