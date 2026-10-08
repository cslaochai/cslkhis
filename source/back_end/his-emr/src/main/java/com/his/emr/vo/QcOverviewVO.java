package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 质控概览（质控工作台顶部卡片）。
 */
@Data
public class QcOverviewVO {

    /**
     * 质控单总数
     */
    private long total;

    /**
     * 待处理
     */
    private long pendingCount;

    /**
     * 不通过
     */
    private long failedCount;

    /**
     * 通过
     */
    private long passedCount;

    /**
     * 命中否决项的质控单数
     */
    private long vetoCount;

    /**
     * 有得分的**综合质控**单数（平均分与甲级率的分母）
     */
    private long scoredCount;

    /**
     * 没有得分的质控单数（旧版质控 + AI 内涵质控；不参与平均分）
     */
    private long unscoredCount;

    /**
     * 平均分（仅统计有得分的综合质控单）；样本为 0 时为 null
     */
    private Double avgScore;

    /**
     * 甲级份数
     */
    private long gradeACount;

    /**
     * 乙级份数
     */
    private long gradeBCount;

    /**
     * 丙级份数
     */
    private long gradeCCount;

    /**
     * 甲级率（甲级 / 有得分的综合质控单）；分母为 0 时为 null，不是 0
     */
    private Double gradeARate;

    /**
     * 问题明细总条数
     */
    private long issueCount;

    /**
     * 问题明细涉及病历数
     */
    private long issueRecordCount;

    /**
     * 维度分布（问题条数）
     */
    private List<DimensionStat> dimensionIssues = new ArrayList<>();

    /**
     * 最近一次质控时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastQcTime;

    @Data
    public static class DimensionStat {

        /**
         * 维度
         */
        private Integer dimension;

        private String dimensionText;

        /**
         * 问题数
         */
        private long issueCount;

        private long deductTotal;
    }
}
