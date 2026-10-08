package com.his.charge.support;

import com.his.common.enums.PaymentItemTypeEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 记账行的医保目录类别推定（0-自费 1-甲类 2-乙类 3-丙类）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeeCatalogResolver {

    /**
     * 自费：不参与统筹分摊
     */
    public static final int SELF_PAY = 0;
    /**
     * 甲类：全额纳入报销范围（按政策比例）
     */
    public static final int CLASS_A = 1;
    /**
     * 乙类：先由个人负担先行自付比例，剩余再按比例报销
     */
    public static final int CLASS_B = 2;

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
