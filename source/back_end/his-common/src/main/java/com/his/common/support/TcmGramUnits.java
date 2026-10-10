package com.his.common.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 中药饮片「克 ↔ 档案单位」换算
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TcmGramUnits {

    /**
     * 每克单价的精度：饮片档案价多为「几十~几百元/kg」，除以 1000 后需要 3~4 位才不会把 0.105 截成 0.11
     */
    private static final int PRICE_SCALE = 4;

    /**
     * 库存列是 decimal(10,2)，扣减量只能精确到 0.01 档案单位
     */
    private static final int STOCK_SCALE = 2;

    /**
     * 该药是否按克开方。换算率为空 = 西药/中成药（或档案没维护），一律走原来的「数量即库存数量」口径。
     */
    public static boolean gramDosed(BigDecimal gramPerUnit) {
        return gramPerUnit != null && gramPerUnit.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 每克单价 = 档案零售价 ÷ 换算率。不按克开方的药原样返回档案价。
     */
    public static BigDecimal perGramPrice(BigDecimal retailPrice, BigDecimal gramPerUnit) {
        if (retailPrice == null || !gramDosed(gramPerUnit)) {
            return retailPrice;
        }
        return retailPrice.divide(gramPerUnit, PRICE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 金额 = 单价 × 克数，四舍五入到分。
     */
    public static BigDecimal amountOf(BigDecimal price, BigDecimal quantity) {
        if (price == null || quantity == null) {
            return null;
        }
        return price.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 克数 → 库存档案单位数量（扣库/锁库都用它），向上取整到 0.01。
     * 不按克开方的药原样返回（返回同一个值，调用方不必分支）。
     */
    public static BigDecimal toStockUnits(BigDecimal grams, BigDecimal gramPerUnit) {
        if (grams == null || !gramDosed(gramPerUnit)) {
            return grams;
        }
        return grams.divide(gramPerUnit, STOCK_SCALE, RoundingMode.CEILING);
    }
}
