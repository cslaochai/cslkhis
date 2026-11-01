package com.his.clinical.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 病案质量指标看板。
 *
 * <p>口径说明（页面要原样展示，不允许只给数字不说口径）：
 * <ul>
 *     <li>通过率 = 最高严重度 &lt; 2 的质控单 / 质控单总数</li>
 *     <li>甲级率 = 无重要及以上问题且得分 ≥ 90 / 质控单总数</li>
 *     <li>时限达标率 = 1 - 命中入院记录 24h、首次病程 8h、出院记录 24h 任一规则的住院文书数 / 住院质控文书数</li>
 *     <li>首页完整率 = 主诊断、主诊断编码、入院途径齐全的首页 / 首页总数</li>
 * </ul>
 * 分母为 0 时比率给 null 而不是 0：没有数据不等于 0 分。
 */
@Data
@Schema(description = "病案质量指标")
public class QcIndicatorVO {

    /**
     * 统计窗口（天）
     */
    private Integer days;

    /**
     * 质控单总数
     */
    private Long totalQc;

    /**
     * 通过数（无重要及以上问题）
     */
    private Long passQc;

    /**
     * 甲级数（无重要及以上问题且得分 ≥ 90）
     */
    private Long gradeAQc;

    /**
     * 乙级数（无重要及以上问题且 75 ≤ 得分 &lt; 90）
     */
    private Long gradeBQc;

    /**
     * 有否决项（严重度 3）的质控单数
     */
    private Long vetoQc;

    /**
     * 平均分
     */
    private BigDecimal avgScore;

    /**
     * 通过率（%）
     */
    private BigDecimal passRate;

    /**
     * 甲级率（%）
     */
    private BigDecimal gradeARate;

    /**
     * 住院质控文书数（时限达标率分母）
     */
    private Long inpatientQc;

    /**
     * 命中时限规则（F06/F07/F08）的住院文书数
     */
    private Long deadlineViolation;

    /**
     * 时限达标率（%）
     */
    private BigDecimal deadlineRate;

    /**
     * 病案首页总数
     */
    private Long summaryTotal;

    /**
     * 首页主诊断/诊断编码/入院途径齐全数
     */
    private Long summaryComplete;

    /**
     * 首页完整率（%）
     */
    private BigDecimal summaryCompleteRate;

    /**
     * 已出院病历数（7 日归档率分母，按住院登记去重）
     */
    private Long archiveTotal;

    /**
     * 出院后 7 日内归档的病历数
     */
    private Long archiveOnTime;

    /**
     * 7 日归档率（%）
     */
    private BigDecimal archiveRate;

    /**
     * 已提交及以上的住院文书数（签名完整率分母）
     */
    private Long signTotal;

    /**
     * 其中已完成电子签名的文书数
     */
    private Long signDone;

    /**
     * 签名完整率（%）
     */
    private BigDecimal signRate;

    /**
     * 科室排名（前 20）
     */
    private List<QcDeptIndicatorVO> deptRanks = new ArrayList<>();
}
