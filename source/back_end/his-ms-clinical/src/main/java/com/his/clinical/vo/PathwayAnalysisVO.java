package com.his.clinical.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 临床路径变异分析 VO：按模板聚合 + 类型分布 + 原因 TOP。
 */
@Data
public class PathwayAnalysisVO implements Serializable {

    /**
     * 汇总（当前筛选范围内）
     */
    private Long enrollCount;
    /** 完成（出径）例数（入径状态 2-已完成） */
    private Long finishCount;
    /** 退径例数（入径状态 3-已退径） */
    private Long abortCount;
    /** 变异登记累计条数（各入径记录上的变异次数求和） */
    private Long varianceCount;
    /** 完成率（百分比，1 位小数；入径例数为 0 时为 null） */
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
