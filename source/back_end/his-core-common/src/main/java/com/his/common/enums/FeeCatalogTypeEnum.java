package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 医保目录类别枚举（0-自费 1-甲类 2-乙类 3-丙类）。
 */
public enum FeeCatalogTypeEnum {

    /**
     * 自费：不参与统筹分摊
     */
    SELF_PAY(0, "自费"),
    /**
     * 甲类：全额纳入报销范围（按政策比例）
     */
    CLASS_A(1, "甲类"),
    /**
     * 乙类：先由个人负担先行自付比例，剩余再按比例报销
     */
    CLASS_B(2, "乙类"),
    /**
     * 丙类：医保不承担，全额由个人支付
     */
    CLASS_C(3, "丙类");

    private final Integer code;
    private final String desc;

    FeeCatalogTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 按项目类型推定目录类别：药品（西药/中成药/中药饮片）乙类，其余项目甲类。
     *
     * <p>报销比例为 0 的政策下，甲类与自费的算式结果相同（统筹 = 金额 × 0），
     * 所以这个推定不会比旧实现多报销一分钱。
     */
    public static int byItemType(Integer itemTypeCode) {
        PaymentItemTypeEnum itemType = PaymentItemTypeEnum.fromCode(itemTypeCode);
        if (itemType == null) {
            return SELF_PAY.code;
        }
        return switch (itemType) {
            case WESTERN_MEDICINE, CHINESE_PATENT_MEDICINE, CHINESE_HERBAL_MEDICINE -> CLASS_B.code;
            case REGISTRATION_FEE -> SELF_PAY.code;
            default -> CLASS_A.code;
        };
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static FeeCatalogTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (FeeCatalogTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        FeeCatalogTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        FeeCatalogTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }
}
