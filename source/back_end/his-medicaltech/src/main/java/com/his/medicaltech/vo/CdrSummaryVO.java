package com.his.medicaltech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * CDR 概览：时间轴的"目录级"数字。
 */
@Data
@Schema(description = "CDR 概览")
public class CdrSummaryVO {

    /** 就诊次数 */
    @Schema(description = "就诊次总数")
    private Integer visitCount;

    /** 门诊就诊次数 */
    @Schema(description = "门诊就诊次")
    private Integer outpatientCount;

    /** 住院次数 */
    @Schema(description = "住院次数")
    private Integer inpatientCount;

    /** 急诊次数 */
    @Schema(description = "急诊次数")
    private Integer emergencyCount;

    /** 在院次数（节点状态为"在院"、尚未出院） */
    @Schema(description = "在院次数（未出院）")
    private Integer activeInpatientCount;

    /** 已归位到就诊次节点的事件总数 */
    @Schema(description = "事件总数")
    private Integer eventCount;

    /** 未能归属到任何就诊次的事件数（>0 说明有单据悬空） */
    @Schema(description = "未能归属到任何就诊次的事件数（>0 说明有单据悬空）")
    private Integer unresolvedEventCount;

    /** 首次就诊时间（结诊时由就诊域回写，取最早结诊时刻） */
    @Schema(description = "首次来院时间")
    private String firstVisitTime;

    /** 最近一次来院时间（取最后一个就诊次节点的开始时间） */
    @Schema(description = "最近一次来院时间")
    private String lastVisitTime;

    /** 合计金额 */
    @Schema(description = "累计费用")
    private BigDecimal totalAmount;

    /** 按事件类型汇总的条数（只统计已归位到就诊次的事件，用于与源数据对账） */
    @Schema(description = "各类事件计数（与源表对账用）")
    private List<CdrCountVO> eventCounts;
}
