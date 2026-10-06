package com.his.medicaltech.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Westgard 多规则质控判定引擎（LIS 室内质控）
 *
 * <p>规则（全部**服务端算**，前端只展示回传的 status 与 violatedRules ——
 * 前端自己判在控/失控，等于把质控结论交给浏览器，改一下 JS 就能把失控改成在控）：
 * <ul>
 *   <li>1-3s：|Z| ≥ 3 → 失控（随机误差过大）</li>
 *   <li>2-2s：连续 2 点同侧且 |Z| ≥ 2 → 失控（系统误差）</li>
 *   <li>R-4s：同一批次内两水平一正一负、|Z1 - Z2| ≥ 4 → 失控（随机误差）</li>
 *   <li>4-1s：连续 4 点同侧且 |Z| ≥ 1 → 失控（系统误差）</li>
 *   <li>10-x：连续 10 点落在均数同一侧 → 失控（系统偏移）</li>
 *   <li>1-2s：|Z| ≥ 2 → 警告（仅当没有失控规则命中时才算警告）</li>
 *   <li>7-T ：连续 7 点单调上升/下降 → 警告（趋势性漂移）</li>
 * </ul>
 *
 * <p>Z =（测定值 − 靶值）/ SD。SD = 0 或靶值为空时无法判定 → 落在「未判定」，
 * **绝不因为算不出就说在控**（算不出却判在控，比报错更危险）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WestgardRuleEngine {

    public static final int IN_CONTROL = 1;
    public static final int WARNING = 2;
    public static final int OUT_OF_CONTROL = 3;
    public static final int UNJUDGED = 0;

    /**
     * @param historyAsc  该计划的历史质控点（**按时间升序**，不含当前点）
     * @param currentZ    当前点的 Z 值
     * @param batchOthers 同一批次（同项目+同仪器+同日期）其它水平的最近点，用于 R-4s；可为 null
     * @return 判定结果
     */
    public static Verdict evaluate(List<QcPoint> historyAsc, double currentZ, List<QcPoint> batchOthers) {
        Verdict v = new Verdict();
        List<QcPoint> seq = new ArrayList<>(historyAsc == null ? new ArrayList<>() : historyAsc);
        seq.add(new QcPoint(currentZ));

        // 1-3s：单点失控
        if (Math.abs(currentZ) >= 3) {
            v.rules.add("1-3s");
        }
        // 2-2s：连续两点同侧超 2s
        if (Math.abs(currentZ) >= 2 && seq.size() >= 2) {
            QcPoint prev = seq.get(seq.size() - 2);
            if (Math.abs(prev.getZ()) >= 2 && sameSide(prev.getZ(), currentZ)) {
                v.rules.add("2-2s");
            }
        }
        // R-4s：同批次两水平一正一负且差值 ≥ 4s
        if (batchOthers != null) {
            for (QcPoint o : batchOthers) {
                if (!sameSide(o.getZ(), currentZ) && Math.abs(currentZ - o.getZ()) >= 4) {
                    v.rules.add("R-4s");
                    break;
                }
            }
        }
        // 4-1s：连续四点同侧超 1s
        if (Math.abs(currentZ) >= 1 && consecutiveSameSideOver(seq, 4, 1.0)) {
            v.rules.add("4-1s");
        }
        // 10-x：连续十点同侧
        if (consecutiveSameSideOver(seq, 10, 0.0)) {
            v.rules.add("10-x");
        }

        if (!v.rules.isEmpty()) {
            v.status = OUT_OF_CONTROL;
            return v;
        }

        // 无失控规则命中才看警告级规则
        if (Math.abs(currentZ) >= 2) {
            v.rules.add("1-2s");
            v.status = WARNING;
            return v;
        }
        if (monotonicRun(seq, 7)) {
            v.rules.add("7-T");
            v.status = WARNING;
        }
        return v;
    }

    /**
     * Z 值：SD 为 0 / 空时返回 null，由调用方落到「未判定」
     */
    public static BigDecimal zScore(BigDecimal value, BigDecimal mean, BigDecimal sd) {
        if (value == null || mean == null || sd == null) {
            return null;
        }
        if (sd.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return value.subtract(mean).divide(sd, 3, RoundingMode.HALF_UP);
    }

    private static boolean sameSide(double a, double b) {
        return (a >= 0 && b >= 0) || (a < 0 && b < 0);
    }

    /**
     * 末尾连续 n 个点同侧且 |Z| ≥ threshold
     */
    private static boolean consecutiveSameSideOver(List<QcPoint> seq, int n, double threshold) {
        if (seq.size() < n) {
            return false;
        }
        List<QcPoint> tail = seq.subList(seq.size() - n, seq.size());
        double sign = tail.get(0).getZ() >= 0 ? 1 : -1;
        for (QcPoint p : tail) {
            if ((p.getZ() >= 0 ? 1 : -1) != sign) {
                return false;
            }
            if (Math.abs(p.getZ()) < threshold) {
                return false;
            }
        }
        return true;
    }

    /**
     * 末尾连续 n 个点单调（全升或全降）
     */
    private static boolean monotonicRun(List<QcPoint> seq, int n) {
        if (seq.size() < n) {
            return false;
        }
        List<QcPoint> tail = seq.subList(seq.size() - n, seq.size());
        boolean up = true;
        boolean down = true;
        for (int i = 1; i < tail.size(); i++) {
            if (tail.get(i).getZ() <= tail.get(i - 1).getZ()) {
                up = false;
            }
            if (tail.get(i).getZ() >= tail.get(i - 1).getZ()) {
                down = false;
            }
        }
        return up || down;
    }

    /**
     * 质控点：Z 值 + 水平（用于 R-4s）
     */
    public static class QcPoint {
        private final double z;

        public QcPoint(double z) {
            this.z = z;
        }

        public double getZ() {
            return z;
        }
    }

    /**
     * 判定结果
     */
    public static class Verdict {
        private final List<String> rules = new ArrayList<>();
        private int status = IN_CONTROL;

        public int getStatus() {
            return status;
        }

        public List<String> getRules() {
            return rules;
        }

        public String ruleText() {
            return String.join(",", rules);
        }
    }
}
