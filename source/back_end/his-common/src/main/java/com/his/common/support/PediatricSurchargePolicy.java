package com.his.common.support;

import com.his.common.util.NumUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

/**
 * 儿科医疗服务价格加收政策。
 *
 * <p><b>政策依据</b>：湘医保发〔2025〕58号《关于规范整合综合诊查类医疗服务价格项目的通知》
 * （长沙市医保局同文，2026-01-01 执行）附件1「门诊诊查费(普通门诊)」011102020010000
 * 计价说明列明确写「儿童加收30%」。湖南省医疗保障局《医疗服务价格项目立项指南湖南省落地政策解读(第4期)》
 * 第6问给出算法原文：「<b>在实际诊查费基础上加收</b>，比如主任医师诊查费是35元，加上儿童加收
 * 35*0.3=10.5元，患者应支付45.5元」。
 *
 * <p><b>为什么这里是乘而不是另立一个加收项目行</b>：政策原文说的是「在相应价格<b>基础上</b>加收」，
 * 不是「另收一项儿童诊查费」。医保结算时门诊诊查费仍按 011102020010000 一个项目报，
 * 加收后金额并入同一行。若拆成两行，第二行既没有国家编码可报、又会重复计一次丙类自付，
 * 结算对不上账。解读第6问的算法（base × 1.3）是唯一口径。
 *
 * <p><b>适用年龄口径</b>：六周岁以内（含6周岁）儿童。湖南省医保局2026-09-09答复人大代表原文：
 * 湘医保发〔2020〕51号「对包括『门诊诊查费』在内的131个医疗服务价格项目设置『六岁以内儿童
 * 在相应价格基础上加收』的价格政策，其中门诊诊查费加收50%」；湘医保发〔2025〕58号
 * 「在延续了门诊诊查费儿童加收政策的同时，将原『门诊诊查费』儿童加收比率从50%调整为30%」。
 * 本类是58号文之后的价格，<b>30% 才是当前值，50% 是2025-12-31前的旧值</b>。
 *
 * <p><b>年龄按哪天算</b>：按<b>就诊日</b>算，不按挂号当天。挂号可提前数天甚至数周预约，
 * 一个今天6岁半的孩子预约到两周后就诊时已满7周岁——按挂号日算会多收，按就诊日算才对得上
 * 实际提供服务那天患者的年龄。这是收费类项目最容易踩错的一个时间基准。
 */
public final class PediatricSurchargePolicy {

    /**
     * 儿童加收比率 30%。
     *
     * <p>依据：湘医保发〔2025〕58号附件1 计价说明「儿童加收30%」。
     * 历史值 50%（湘医保发〔2020〕51号）已于2025-12-31随旧价格项目废止，不要再引 0.5。
     */
    public static final BigDecimal RATE = new BigDecimal("0.30");

    /**
     * 加收适用年龄上限：六周岁（含）。即 6 岁 0 个月 ~ 6 岁 11 个月加收，满 7 周岁不再加收。
     */
    public static final int MAX_AGE_YEARS = 6;

    private PediatricSurchargePolicy() {
    }

    /**
     * 是否适用儿童加收。
     *
     * <p><b>出生日期缺失时返回 false（不加收）</b>：判不出来就不加收。多收钱会被医保稽核抓、
     * 会引发投诉和退费；少收只是少收，且出生日期缺失本身是建档质量问题，
     * 该在质控里追而不是在收费口硬猜一个年龄。
     *
     * @param birthDate 患者出生日期，可为 null
     * @param visitDate 实际就诊日（不是挂号日），可为 null
     * @return true = 六周岁以内，需加收 30%
     */
    public static boolean isChild(LocalDate birthDate, LocalDate visitDate) {
        if (birthDate == null || visitDate == null) {
            return false;
        }
        if (birthDate.isAfter(visitDate)) {
            // 出生日晚于就诊日：数据错误（未来出生 / 就诊日录错），当成年的算，判不出儿童
            return false;
        }
        return Period.between(birthDate, visitDate).getYears() <= MAX_AGE_YEARS;
    }

    /**
     * 计算含儿童加收的应收诊查费。
     *
     * @param baseAmount 号源原价（已含职级加收，如知名专家 9+61=70）
     * @param birthDate  出生日期
     * @param visitDate  就诊日
     * @return 儿童：原价×1.3；非儿童/判不出：原价原样返回
     */
    public static BigDecimal apply(BigDecimal baseAmount, LocalDate birthDate, LocalDate visitDate) {
        BigDecimal base = NumUtil.orZero(baseAmount);
        if (base.signum() <= 0 || !isChild(birthDate, visitDate)) {
            return base;
        }
        return base.add(surchargeOf(base));
    }

    /**
     * 儿童加收额本身（base × 30%）。前端要显示「原价 + 加收」明细时用这个，别让前端自己算 0.3。
     *
     * <p>金额保留 2 位、HALF_UP：38.5 × 0.3 = 11.55 这类值必须落到分，不能带着 3 位小数进账单
     * （账单汇总时按行相加会出现分位对不齐）。价格类计算统一走 NumUtil.scale。
     */
    public static BigDecimal surchargeOf(BigDecimal baseAmount) {
        BigDecimal base = NumUtil.orZero(baseAmount);
        if (base.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return base.multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 儿童加收的说明文案，写进收费单备注，供事后追溯「这笔为什么比号源原价高」。
     */
    public static String reasonText(LocalDate birthDate, LocalDate visitDate) {
        if (birthDate == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("六周岁以内儿童加收").append(RATE.movePointRight(2).stripTrailingZeros().toPlainString())
                .append("%");
        if (visitDate != null) {
            int years = Period.between(birthDate, visitDate).getYears();
            sb.append("（就诊日 ").append(visitDate).append(" 实龄 ").append(years).append(" 岁）");
        }
        sb.append("：湘医保发〔2025〕58号附件1计价说明");
        return sb.toString();
    }
}