package com.his.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * **患者全景时间轴**（CDR 的输出物）。
 *
 * <p>结构：身份卡 → 概览 → 就诊次节点（含事件与缺口） → 未能归属的事件。
 * 最后一段是刻意留的：宁可让"归属不上的单据"显眼地堆在那里，也不让它悄悄消失。
 */
@Data
@Schema(description = "患者全景时间轴")
public class CdrTimelineVO {

    @Schema(description = "患者身份卡")
    private CdrPatientVO patient;

    /** 小结 */
    @Schema(description = "概览")
    private CdrSummaryVO summary;

    @Schema(description = "就诊次节点（按开始时间倒序）")
    private List<CdrVisitNodeVO> visits;

    @Schema(description = "未能归属到就诊次的事件")
    private List<CdrEventVO> unresolvedEvents;

    @Schema(description = "健康档案（过敏史 / 既往史 / 手术史 / 家族史 / 用药史 / 联系人）")
    private List<CdrProfileGroupVO> profile;

    @Schema(description = "整体提示（如「本页含 2 份档案的数据」）")
    private List<String> warnings;
}
