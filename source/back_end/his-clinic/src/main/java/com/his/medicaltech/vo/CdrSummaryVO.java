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

    @Schema(description = "就诊次总数")
    private Integer visitCount;

    @Schema(description = "门诊就诊次")
    private Integer outpatientCount;

    @Schema(description = "住院次数")
    private Integer inpatientCount;

    @Schema(description = "急诊次数")
    private Integer emergencyCount;

    @Schema(description = "在院次数（未出院）")
    private Integer activeInpatientCount;

    @Schema(description = "事件总数")
    private Integer eventCount;

    @Schema(description = "未能归属到任何就诊次的事件数（>0 说明有单据悬空）")
    private Integer unresolvedEventCount;

    @Schema(description = "首次来院时间")
    private String firstVisitTime;

    @Schema(description = "最近一次来院时间")
    private String lastVisitTime;

    /** 合计金额 */
    @Schema(description = "累计费用")
    private BigDecimal totalAmount;

    @Schema(description = "各类事件计数（与源表对账用）")
    private List<CdrCountVO> eventCounts;
}
