package com.his.fee.support;

import com.his.common.enums.PaymentItemTypeEnum;

/**
 * 记账行的<b>医保目录类别</b>推定（0-自费 1-甲类 2-乙类 3-丙类）。
 *
 * <p>本系统没有院内医保目录表（学习阶段不做真实外部对接），所以目录类别只能按项目类型推定，
 * 口径与旧收费实现完全一致（{@code ChargeServiceImpl} 当年在结算时贴标：药品乙类、其余甲类），
 * 只是把它挪回<b>记账</b>这一刻：目录类别是项目的属性，不该由"这个患者报销比例是多少"倒推。
 *
 * <p>接进真目录表之后，只需让各来源在 {@code FeeBookDTO.catalogType} 里传真实码值，
 * 本类推定退化为兜底。
 */
public final class FeeCatalogResolver {

    /** 自费：不参与统筹分摊 */
    public static final int SELF_PAY = 0;
    /** 甲类：全额纳入报销范围（按政策比例） */
    public static final int CLASS_A = 1;
    /** 乙类：先由个人负担先行自付比例，剩余再按比例报销 */
    public static final int CLASS_B = 2;

    private FeeCatalogResolver() {
    }

    /**
     * 按项目类型推定目录类别：药品（西药/中成药/中药饮片）乙类，其余项目甲类。
     *
     * <p>报销比例为 0 的政策下，甲类与自费的算式结果相同（统筹 = 金额 × 0），
     * 所以这个推定不会比旧实现多报销一分钱。
     */
    public static int byItemType(Integer itemTypeCode) {
        PaymentItemTypeEnum itemType = PaymentItemTypeEnum.getByCode(itemTypeCode);
        if (itemType == null) {
            return SELF_PAY;
        }
        return switch (itemType) {
            case WESTERN_MEDICINE, CHINESE_PATENT_MEDICINE, CHINESE_HERBAL_MEDICINE -> CLASS_B;
            case REGISTRATION_FEE -> SELF_PAY;
            default -> CLASS_A;
        };
    }
}
