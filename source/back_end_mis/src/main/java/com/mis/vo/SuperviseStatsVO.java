package com.mis.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 基金监管工作台统计
 */
@Data
public class SuperviseStatsVO implements Serializable {

    /** 启用规则数 */
    private long ruleCount;
    /** 疑点总数 */
    private long suspicionTotal;
    /** 待核实疑点数 */
    private long suspicionPending;
    /** 已确认疑点数 */
    private long suspicionConfirmed;
    /** 已排除疑点数 */
    private long suspicionExcluded;
    /** 已确认违规金额合计 */
    private BigDecimal confirmedAmount;
    /** 待追回金额 */
    private BigDecimal recoverPendingAmount;
    /** 已追回金额 */
    private BigDecimal recoveredAmount;
    /** 已核销金额 */
    private BigDecimal writeOffAmount;
    /** 检查中任务数 */
    private long inspectionRunning;
    /** 已完成任务数 */
    private long inspectionDone;
    /** 按规则分布 */
    private List<RuleDistVO> ruleDist;
    /** 近 10 日疑点趋势 */
    private List<TrendPointVO> trend;

    @Data
    public static class RuleDistVO implements Serializable {
        private String ruleName;
        private String ruleType;
        private long count;
        private BigDecimal amount;
    }

    @Data
    public static class TrendPointVO implements Serializable {
        private String day;
        private long count;
        private BigDecimal amount;
    }
}
