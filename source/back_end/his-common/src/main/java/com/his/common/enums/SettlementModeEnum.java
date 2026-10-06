package com.his.common.enums;

/**
 * 结算方式枚举
 */
public enum SettlementModeEnum {

    SELF_PAY(1, "自费"),
    INSURANCE(2, "医保");

    private final Integer code;
    private final String desc;

    SettlementModeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SettlementModeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SettlementModeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        SettlementModeEnum item = getByCode(code);
        return item == null ? "" : item.desc;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        SettlementModeEnum item = code == null ? null : getByCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
