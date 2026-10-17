package com.his.common.enums;

import lombok.Getter;

/**
 * 人力需求来源枚举（1-门诊出诊派生 2-住院患者派生 3-手工调整）
 */
@Getter
public enum StaffDemandSourceEnum {

    CLINIC(1, "门诊出诊派生"),
    INPATIENT(2, "住院患者派生"),
    MANUAL(3, "手工调整");

    private final int code;
    private final String label;

    StaffDemandSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StaffDemandSourceEnum fromCode(int code) {
        for (StaffDemandSourceEnum source : values()) {
            if (source.code == code) return source;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        StaffDemandSourceEnum source = code == null ? null : fromCode(code);
        return source == null ? "" : source.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        StaffDemandSourceEnum source = code == null ? null : fromCode(code);
        if (source != null) {
            return source.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
