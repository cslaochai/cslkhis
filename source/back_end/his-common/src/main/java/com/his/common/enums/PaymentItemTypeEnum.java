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

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        PaymentItemTypeEnum item = getByCode(code);
        return item == null ? "" : item.desc;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        PaymentItemTypeEnum item = getByCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）。
     * 结算账单明细的项目类型落库码值 1-8 全在本枚举内（2026-10-06 核实）。
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}