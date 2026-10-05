package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 满意度看板统计（服务端 group by 出）。
 *
 * <p>绝不让前端拿「当前页 list」去数 —— 那等于只统计了本页，翻页就变（与纠纷统计同一口径）。
 */
@Data
public class SurveyStatVO implements Serializable {

    // 回收侧（分母是发放行，不是答卷行）

    /**
     * 发放总数
     */
    private Long dispatchTotal;

    /**
     * 已回收
     */
    private Long recycledCount;

    /**
     * 待推送
     */
    private Long pendingPushCount;

    /**
     * 已推送待回收
     */
    private Long waitingCount;

    /**
     * 已拒答
     */
    private Long refusedCount;

    /**
     * 超截止仍未回收（现算：dispatch_status<3 且 expire_time<now）
     */
    private Long overdueCount;

    /**
     * 回收率 = 已回收 / 发放总数（分母含未回收，这是它唯一有意义的算法）
     */
    private BigDecimal recycleRate;

    // 答卷侧

    /**
     * 有效答卷数
     */
    private Long answerTotal;

    /**
     * 作废答卷数（不进统计，但要露出来）
     */
    private Long voidCount;

    /**
     * 李克特均分（1.00~5.00）
     */
    private BigDecimal avgScore;

    /**
     * 百分制得分（评审上报口径）
     */
    private BigDecimal avgScore100;

    /**
     * 满意率 =（均分>=4 的答卷数）/ 有效答卷数
     */
    private BigDecimal satisfiedRate;

    /**
     * 低分答卷数（百分制<60 或某维度均分<=2）
     */
    private Long lowScoreCount;

    /**
     * 其中已自动转出投诉的答卷数
     */
    private Long disputedCount;

    /**
     * NPS 净推荐值 =（推荐者 9-10 − 贬损者 0-6）/ 有 NPS 答卷数 ×100
     */
    private BigDecimal nps;

    // 分布

    /**
     * 各评价维度均分（升序 = 短板在前）
     */
    private List<SurveyStatItemVO> byDimension;

    /**
     * 科室短板 TOP10（按百分制均分升序）
     */
    private List<SurveyStatItemVO> byDeptBottom;

    /**
     * 各回收渠道的发放数与回收率（把「发出去永远收不回」的渠道露出来）
     */
    private List<SurveyStatItemVO> byChannel;

    /**
     * 近 30 日趋势（date / 答卷数 / 百分制均分）
     */
    private List<SurveyStatItemVO> byDay;
}
