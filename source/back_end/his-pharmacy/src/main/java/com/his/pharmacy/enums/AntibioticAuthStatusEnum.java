package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 抗菌药物授权状态枚举（1-有效 2-暂停 3-取消，口径 = biz_antibiotic_auth.status）。
 */
@Getter
public enum AntibioticAuthStatusEnum {

    VALID(1, "有效"),
    PAUSED(2, "暂停"),
    REVOKED(3, "取消");

    private final int code;
    private final String label;

    AntibioticAuthStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AntibioticAuthStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AntibioticAuthStatusEnum item : values()) {
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
        AntibioticAuthStatusEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        AntibioticAuthStatusEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
