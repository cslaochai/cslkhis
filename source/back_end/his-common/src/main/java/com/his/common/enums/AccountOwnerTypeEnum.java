package com.his.common.enums;

/**
 * 资金账户主体（字典 his_account_owner_type，落在资金账户的主体类型列）。
 */
public enum AccountOwnerTypeEnum {

    PATIENT(1, "患者（门诊余额）"),
    ADMISSION(2, "住院就诊次（预交金）");

    private final Integer code;
    private final String desc;

    AccountOwnerTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AccountOwnerTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AccountOwnerTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String descOf(Integer code) {
        AccountOwnerTypeEnum item = fromCode(code);
        return item == null ? "未知主体" : item.desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
