package com.his.common.enums;

/**
 * 资金账户主体（字典 {@code his_account_owner_type}，落在资金账户的主体类型列）。
 *
 * <p>门诊余额挂在"人"上（今天能抵下次挂号费），住院预交金挂在"这次入院"上
 * （欠费管控、出院退差都按入院算）。两种账户共用一张表，靠主体类型区分，
 * 免得再开一张预交金表、余额口径各算一套。
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
