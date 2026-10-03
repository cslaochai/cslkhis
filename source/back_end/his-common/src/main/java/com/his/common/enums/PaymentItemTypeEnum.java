package com.his.common.enums;

/**
 * 付款项目类型枚举
 */
public enum PaymentItemTypeEnum {

    REGISTRATION_FEE(1, "挂号费"),
    WESTERN_MEDICINE(2, "西药"),
    CHINESE_PATENT_MEDICINE(3, "中成药"),
    CHINESE_HERBAL_MEDICINE(4, "中药饮片"),
    EXAMINATION(5, "检查"),
    LABORATORY_TEST(6, "检验"),
    TREATMENT(7, "治疗"),
    CONSUMABLE(8, "耗材材料");

    private final Integer code;
    private final String desc;

    PaymentItemTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取对应的枚举对象
     *
     * @param code 类型编码
     * @return 对应的枚举对象，未找到返回 null
     */
    public static PaymentItemTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentItemTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}