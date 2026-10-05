package com.his.common.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 中药饮片「克 ↔ 档案单位」换算（sql/139 第三条口径的唯一实现处）。
 *
 * <p>处方上写的是克（每剂克数 × 剂数），库存批次记的是档案单位（散装饮片是 kg、
 * 包装饮片是袋），两者之间只有药品字典的每单位克数列一个换算率来源。
 * 谁都不许去 parse {@code specification} 文本 —— '统货'/'10g/袋'/'500g/袋' 混着写，
 * 解析迟早漏一种，而且漏了不报错，只是账上少扣或多扣。
 *
 * <p>取整方向是<b>有意不对称</b>的：金额四舍五入（对患者的零头不往上加），
 * 扣库存<b>向上</b>取整到 0.01 档案单位（向下取整等于少扣库存，账上永远比架上多，
 * 拆零发出去的半袋也要从架上拿走）。
 */
public final class TcmGramUnits {

    /**
     * 每克单价的精度：饮片档案价多为「几十~几百元/kg」，除以 1000 后需要 3~4 位才不会把 0.105 截成 0.11
     */
    private static final int PRICE_SCALE = 4;

    /**
     * 库存列是 decimal(10,2)，扣减量只能精确到 0.01 档案单位
     */
    private static final int STOCK_SCALE = 2;

    private TcmGramUnits() {
    }

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
