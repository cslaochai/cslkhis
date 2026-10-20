package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 付款项目类型枚举
 *
 * <p>注意：这里是<b>项目分类</b>（把账单明细按大类归口），不是价格项目名称。
 * 码值 1 的文案用「门诊诊查费」而不是「挂号费」：湘医保发〔2025〕58号已废止挂号费合并入门诊诊查费，
 * 患者在费用清单里看到「挂号费」会以为还在按废止项目收费。码值 1 本身不变（已落库）。
 */
public enum PaymentItemTypeEnum {

    REGISTRATION_FEE(1, "门诊诊查费"),
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
     * 码值是否合法（写入侧校验用；null 不合法）。
     * 结算账单明细的项目类型落库码值 1-8 全在本枚举内（2026-10-06 核实）。
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PaymentItemTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PaymentItemTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PaymentItemTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PaymentItemTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        PaymentItemTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}