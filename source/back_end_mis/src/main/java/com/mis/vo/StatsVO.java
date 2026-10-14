package com.mis.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计报表：按险种 / 科室聚合 + 近 10 日趋势
 * （byIns / byDept 的 key 为动态字典值，属分组聚合 Map，非隐式数据契约）
 */
@Data
public class StatsVO implements Serializable {

    private Map<String, InsStat> byIns;
    private Map<String, DeptStat> byDept;
    private List<TrendPoint> trend;

    @Data
    public static class InsStat implements Serializable {
        private int count;
        private BigDecimal amount;
        private BigDecimal insurancePay;
    }

    @Data
    public static class DeptStat implements Serializable {
        private int count;
        private BigDecimal amount;
    }

    @Data
    public static class TrendPoint implements Serializable {
        private String day;
        private int count;
        private BigDecimal amount;
    }
}
