package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 临床路径变异分析 VO：按模板聚合 + 类型分布 + 原因 TOP。
 *
 * <p>完成率 = 完成数 / 入径数（退径不计入分子），百分数保留 1 位小数。
 */
@Data
public class PathwayAnalysisVO implements Serializable {

    /**
     * 汇总（当前筛选范围内）
     */
    private Long enrollCount;
    private Long finishCount;
    private Long abortCount;
    private Long varianceCount;
    private Double finishRate;

    /**
     * 模板维度行
     */
    private List<Row> rows;

    /**
     * 变异类型分布
     */
    private List<TypeStat> typeStats;

    /**
     * 变异原因 TOP10
     */
    private List<ReasonStat> topReasons;

    @Data
    public static class Row implements Serializable {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long pathwayId;
        private String pathwayCode;
        private String pathwayName;
        private String version;
        private Long enrollCount;
        private Long finishCount;
        private Long abortCount;
        private Long varianceCount;
        private Double finishRate;
    }

    @Data
    public static class TypeStat implements Serializable {
        private Integer varianceType;
        private Long cnt;
    }

    @Data
    public static class ReasonStat implements Serializable {
        /**
         * 原因
         */
        private String reason;
        private Long cnt;
    }
}
