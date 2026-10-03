package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 质控概览（质控工作台顶部卡片）。
 *
 * <p><b>口径必须写清楚，否则这些数字会被误读</b>：
 * <ul>
 *   <li>{@code avgScore} 只对有得分的质控单求平均；旧版质控（score 为 NULL）不计入，
 *       而且 {@code unscoredCount} 必须一并展示 —— 悄悄把 NULL 当 0 算进平均分，
 *       会把平均分拉低成假象。</li>
 *   <li>甲级率的分母是**有得分的综合质控单**，不是全部质控单，也不是所有维度的质控单。</li>
 *   <li><b>平均分与甲级率只统计综合质控（qc_type=0）</b>。完整性 / 规范性 / 逻辑性单独出单时，
 *       分数只代表那一个维度（缺主诉属完整性，单独跑逻辑性根本看不到），
 *       混进来会把甲级率系统性抬高 —— 实测库内 65.4% vs 64.2%，样本越大差距越明显。
 *       「单量类」指标（total / pendingCount / failedCount / passedCount / vetoCount）
 *       仍然统计全部质控单：它们回答的是"有多少张单要处理"。</li>
 *   <li>{@code unscoredCount} 反过来统计**全部**没得分的单（旧版质控 + AI 内涵质控），
 *       它要回答的是"为什么有些行显示 —"。</li>
 *   <li>{@code dimensionIssues} 的维度分布来自问题明细，因此只统计规则引擎产生的质控单
 *       （AI 内涵质控 qc_type=4 不产生明细）。</li>
 * </ul>
 *
 * <p><b>尚未收口的口径</b>：这里算的是"质控单张数"意义上的甲级率。
 * 同一份病历反复质控会重复计入；若要"病历甲级率"，应每份病历取最近一张综合质控单再统计。
 * 那是一次口径变更（数字会变），要单独做并重跑验证，不要顺手改。
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

        /** 维度 */
        private Integer dimension;

        private String dimensionText;

        /** 问题数 */
        private long issueCount;

        private long deductTotal;
    }
}
