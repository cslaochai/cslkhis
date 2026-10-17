package com.his.medicaltech.support;

import com.his.common.enums.EqaJudgeModeEnum;
import com.his.common.enums.EqaResultStatusEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 室间质评（EQA）判定引擎
 */
@NoArgsConstructor
public final class EqaJudgeEngine {

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
            int status = abs.compareTo(SDI_SATISFY) < 0 ? EqaResultStatusEnum.SATISFACTORY.getCode()
                    : (abs.compareTo(SDI_FAIL) < 0 ? EqaResultStatusEnum.ACCEPTABLE.getCode()
                            : EqaResultStatusEnum.FAILED.getCode());
            return new Verdict(EqaJudgeModeEnum.SDI.getCode(), sdi, bias, status);
        }

        if (tea != null && tea.compareTo(BigDecimal.ZERO) > 0 && bias != null) {
            BigDecimal times = bias.abs().divide(tea, 3, RoundingMode.HALF_UP);
            int status = times.compareTo(BigDecimal.ONE) <= 0 ? EqaResultStatusEnum.SATISFACTORY.getCode()
                    : (times.compareTo(TEA_WARN_TIMES) <= 0 ? EqaResultStatusEnum.ACCEPTABLE.getCode()
                            : EqaResultStatusEnum.FAILED.getCode());
            return new Verdict(EqaJudgeModeEnum.TEA.getCode(), null, bias, status);
        }

        if (min != null && max != null) {
            boolean inRange = testValue.compareTo(min) >= 0 && testValue.compareTo(max) <= 0;
            if (inRange) {
                return new Verdict(EqaJudgeModeEnum.RANGE.getCode(), null, bias,
                        EqaResultStatusEnum.SATISFACTORY.getCode());
            }
            if (tea != null && tea.compareTo(BigDecimal.ZERO) > 0 && bias != null
                    && bias.abs().compareTo(tea) <= 0) {
                return new Verdict(EqaJudgeModeEnum.RANGE.getCode(), null, bias,
                        EqaResultStatusEnum.ACCEPTABLE.getCode());
            }
            return new Verdict(EqaJudgeModeEnum.RANGE.getCode(), null, bias,
                    EqaResultStatusEnum.FAILED.getCode());
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
         * 缺少判定依据时的兜底：口径为无法判定 + 结果为未判定，前端显示「未判定」
         */
        public static Verdict none() {
            return new Verdict(EqaJudgeModeEnum.NONE.getCode(), null, null,
                    EqaResultStatusEnum.UNJUDGED.getCode());
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
