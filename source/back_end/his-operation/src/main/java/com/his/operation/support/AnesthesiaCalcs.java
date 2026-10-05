package com.his.operation.support;

import com.his.common.enums.YesOrNoEnum;
import com.his.operation.enums.AsaGradeEnum;

import java.math.BigDecimal;
import java.util.List;

/**
 * 手术麻醉链的纯业务计算口径（Aldrete 总分 / 记账折算 / 失败原因截断 / 时长格式化）。
 *
 * <p>本类<b>只承载临床判定与计算口径，不承载任何码值 → 文案映射</b>：
 * 展示文案一律走 {@code com.his.operation.enums} 下各枚举的 {@code getText}（展示）
 * / {@code labelOrUnknown}（异常 / 审计），本类里不得出现「未知(code)」兜底。
 */
public final class AnesthesiaCalcs {

    private AnesthesiaCalcs() {
    }

    /**
     * 出室 Aldrete 评分阈值（≥ 9 才允许按标准出室）。
     *
     * <p>这是临床阈值不是码值，按 §13 留 {@code static final int}，不进枚举。
     */
    public static final int ALDRETE_DISCHARGE_MIN = 9;

    /**
     * ASA 分级 + 急诊 E 标志的完整展示（如「Ⅲ级 E（急诊）」）。
     * 文案来源唯一出口 {@link AsaGradeEnum#getText}；是否急诊的判定用 {@link YesOrNoEnum}。
     */
    public static String asaFullText(Integer grade, Integer emergency) {
        String base = AsaGradeEnum.getText(grade);
        if (Integer.valueOf(YesOrNoEnum.YES.getCode()).equals(emergency)) {
            return base + " E（急诊）";
        }
        return base;
    }

    /**
     * Aldrete 五项逐项相加得总分。
     *
     * <p>任一项为 null 当作「未评分」整体返回 null —— 不能把缺项当成 0 分再算个总分出来，
     * 那样"意识没评"会变成"意识 0 分"，看着像评得很差，实际是根本没评。
     */
    public static Integer aldreteTotal(Integer activity, Integer respiration, Integer circulation,
                                       Integer consciousness, Integer spo2) {
        List<Integer> parts = List.of(activity, respiration, circulation, consciousness, spo2);
        int total = 0;
        for (Integer p : parts) {
            if (p == null) {
                return null;
            }
            if (p < 0 || p > 2) {
                throw new IllegalArgumentException("Aldrete 各项只能在 0~2 分之间，当前值=" + p);
            }
            total += p;
        }
        return total;
    }

    /** 失败原因列宽（手术麻醉计费明细.fail_reason / *.charge_fail_reason 都是 VARCHAR(500)） */
    private static final int FAIL_REASON_MAX = 480;

    /**
     * 失败原因入库前截到列宽。
     *
     * <p>这不是"为了好看"：计费失败原因里会带上数据库/远程调用的原始异常文本，
     * 一次 {@code Data too long} 就会把"记账失败"升级成 500 —— 结果是
     * **失败原因太长导致整条业务写不进去**，用户看到的是白屏而不是"这笔钱没计上"。
     * 记不下的部分丢掉，比整行写不进去强。
     */
    public static String clipReason(String reason) {
        if (reason == null) {
            return null;
        }
        return reason.length() <= FAIL_REASON_MAX ? reason : reason.substring(0, FAIL_REASON_MAX) + "…（已截断）";
    }

    /** 时长文案（分钟 → "1 小时 20 分钟"） */
    public static String durationText(Long minutes) {
        if (minutes == null) {
            return "—";
        }
        if (minutes < 60) {
            return minutes + " 分钟";
        }
        long h = minutes / 60;
        long m = minutes % 60;
        return m == 0 ? h + " 小时" : h + " 小时 " + m + " 分钟";
    }

    /**
     * 向上取整整小时（麻醉监护按小时计价，不足 1 小时按 1 小时）。
     *
     * <p>0 分钟不能折算成 0 小时然后收 0 元 —— 那等于"这台手术没有麻醉监护"，
     * 与"取不到单价就不计费"是同一件事的两面：**要么按事实计费，要么明确报失败**。
     */
    public static BigDecimal billHours(Long minutes) {
        if (minutes == null || minutes <= 0) {
            return BigDecimal.ONE;
        }
        long hours = (minutes + 59) / 60;
        return BigDecimal.valueOf(hours);
    }
}
