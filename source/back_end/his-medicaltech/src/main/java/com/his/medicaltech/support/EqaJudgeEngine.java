package com.his.medicaltech.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 室间质评（EQA）判定引擎
 *
 * <p>和室内质控的 Westgard 一样：**判定只在这里做**。组织方把成绩回报回来之后，
 * 靶值/SD/TEa 是什么、本室偏了多少、合不合格，全部服务端算完落库，前端只负责显示。
 * 前端自己"看着靶值差不离就判合格"，等于把评审证据交给浏览器。
 *
 * <p>三种口径按优先级取一（不能几个口径挑最好的那个用 —— 那是给自己放水）：
 * <ol>
 *   <li><b>SDI</b>：给了组 SD 就用它。SDI =（本室值 − 靶值）/ 组SD。
 *       |SDI| &lt; 1 满意，1 ≤ |SDI| &lt; 2 尚可，≥ 2 不合格。</li>
 *   <li><b>允许总误差 TEa</b>：只有靶值和 TEa 时用。偏倚% =（本室值 − 靶值）/ 靶值 × 100。
 *       |偏倚| ≤ TEa 满意，≤ 1.5×TEa 尚可，超出不合格。</li>
 *   <li><b>可接受范围</b>：组织方只给了上下限时用。范围内满意；
 *       范围外但偏倚还在 TEa 以内记「尚可」（边界擦边，先预警）；否则不合格。</li>
 * </ol>
 * 三个都拿不出数据 → <b>判不出来就是判不出来</b>，result_status 落到 0 未判定，
 * 不因为"看起来差不多"就写合格。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EqaJudgeEngine {

    public static final int MODE_NONE = 0;
    public static final int MODE_SDI = 1;
    public static final int MODE_TEA = 2;
    public static final int MODE_RANGE = 3;

    /**
     * 0-未判定
     */
    public static final int UNJUDGED = 0;
    /**
     * 1-满意
     */
    public static final int SATISFACTORY = 1;
    /**
     * 2-尚可（踩着边界，算合格但要盯着）
     */
    public static final int ACCEPTABLE = 2;
    /**
     * 3-不合格
     */
    public static final int FAILED = 3;

    /**
     * PT 合格红线（%）：一次质评里合格项占比低于这个值，整批就是不合格
     */
    public static final BigDecimal PT_PASS_LINE = new BigDecimal("80");
    /**
     * 没给 TEa 时的默认允许互差（%），用于仪器间比对
     */
    public static final BigDecimal DEFAULT_COMPARE_ALLOW = new BigDecimal("8");
    /**
     * TEa 口径下「尚可」的倍数上限
     */
    private static final BigDecimal TEA_WARN_TIMES = new BigDecimal("1.5");
    private static final BigDecimal SDI_SATISFY = BigDecimal.ONE;
    private static final BigDecimal SDI_FAIL = new BigDecimal("2");

    /**
     * 单条盲样成绩判定。
     *
     * @param testValue 本室测定值（必填，没测就没得判）
     * @param target    回报靶值 / 组均值
     * @param groupSd   回报组标准差
     * @param tea       允许总误差（%）
     * @param min/max   可接受范围
     */
    public static Verdict judge(BigDecimal testValue, BigDecimal target, BigDecimal groupSd,
                                BigDecimal tea, BigDecimal min, BigDecimal max) {
        if (testValue == null) {
            return Verdict.none();
        }
        BigDecimal bias = biasRate(testValue, target);

        if (target != null && groupSd != null && groupSd.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sdi = testValue.subtract(target).divide(groupSd, 3, RoundingMode.HALF_UP);
            BigDecimal abs = sdi.abs();
            int status = abs.compareTo(SDI_SATISFY) < 0 ? SATISFACTORY
                    : (abs.compareTo(SDI_FAIL) < 0 ? ACCEPTABLE : FAILED);
            return new Verdict(MODE_SDI, sdi, bias, status);
        }

        if (tea != null && tea.compareTo(BigDecimal.ZERO) > 0 && bias != null) {
            BigDecimal times = bias.abs().divide(tea, 3, RoundingMode.HALF_UP);
            int status = times.compareTo(BigDecimal.ONE) <= 0 ? SATISFACTORY
                    : (times.compareTo(TEA_WARN_TIMES) <= 0 ? ACCEPTABLE : FAILED);
            return new Verdict(MODE_TEA, null, bias, status);
        }

        if (min != null && max != null) {
            boolean inRange = testValue.compareTo(min) >= 0 && testValue.compareTo(max) <= 0;
            if (inRange) {
                return new Verdict(MODE_RANGE, null, bias, SATISFACTORY);
            }
            if (tea != null && tea.compareTo(BigDecimal.ZERO) > 0 && bias != null
                    && bias.abs().compareTo(tea) <= 0) {
                return new Verdict(MODE_RANGE, null, bias, ACCEPTABLE);
            }
            return new Verdict(MODE_RANGE, null, bias, FAILED);
        }

        return Verdict.none();
    }

    /**
     * 偏倚% =（本室值 − 靶值）/ |靶值| × 100；靶值缺失或为 0 时算不出 → null
     */
    public static BigDecimal biasRate(BigDecimal testValue, BigDecimal target) {
        if (testValue == null || target == null || target.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return testValue.subtract(target)
                .divide(target.abs(), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * PT 得分 = 合格项数（满意 + 尚可）/ 已判定项数 × 100。
     * 分母必须是<b>已判定的</b>项：没回报成绩的不能算分母，否则拿"还没结果"冲抵不合格。
     */
    public static BigDecimal ptScore(int judgedCount, int passCount) {
        if (judgedCount <= 0) {
            return null;
        }
        return BigDecimal.valueOf(passCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(judgedCount), 2, RoundingMode.HALF_UP);
    }

    public static boolean ptPass(BigDecimal score) {
        return score != null && score.compareTo(PT_PASS_LINE) >= 0;
    }

    /**
     * 互差允许限：回报了 TEa 就取它的一半（业内通行做法），没给就用默认值 8%。
     * 来源号必须一起返回给前端 —— 把默认值包装成"依据某标准"，是台账里最难查的假。
     */
    public static BigDecimal allowRate(BigDecimal tea) {
        if (tea != null && tea.compareTo(BigDecimal.ZERO) > 0) {
            return tea.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }
        return DEFAULT_COMPARE_ALLOW;
    }

    /**
     * 相对互差% = |A − B| / |(A + B) / 2| × 100。
     * 均值恰为 0（两台机结果等值反号）时算不出相对量 → 返回 null，调用方按「超差」处理并记录。
     */
    public static BigDecimal diffRate(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return null;
        }
        BigDecimal mean = a.add(b).divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
        if (mean.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return a.subtract(b).abs()
                .divide(mean.abs(), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 判定结果
     */
    public static class Verdict {

        private final int judgeMode;
        private final BigDecimal sdi;
        private final BigDecimal biasRate;
        private final int resultStatus;

        private Verdict(int judgeMode, BigDecimal sdi, BigDecimal biasRate, int resultStatus) {
            this.judgeMode = judgeMode;
            this.sdi = sdi;
            this.biasRate = biasRate;
            this.resultStatus = resultStatus;
        }

        /**
         * 缺少判定依据时的兜底：mode=0 + resultStatus=0，前端显示「未判定」
         */
        public static Verdict none() {
            return new Verdict(MODE_NONE, null, null, UNJUDGED);
        }

        public int getJudgeMode() {
            return judgeMode;
        }

        public BigDecimal getSdi() {
            return sdi;
        }

        public BigDecimal getBiasRate() {
            return biasRate;
        }

        public int getResultStatus() {
            return resultStatus;
        }
    }
}
