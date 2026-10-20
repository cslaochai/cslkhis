package com.his.medicaltech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * **患者全景时间轴**（CDR 的输出物）。
 */
@Data
@Schema(description = "患者全景时间轴")
public class CdrTimelineVO {

    /** 患者身份卡（主档信息 + EMPI 归并情况） */
    @Schema(description = "患者身份卡")
    private CdrPatientVO patient;

    /** 小结 */
    @Schema(description = "概览")
    private CdrSummaryVO summary;

    /** 就诊次节点列表（按开始时间倒序，门诊/住院/急诊+跨就诊轴） */
    @Schema(description = "就诊次节点（按开始时间倒序）")
    private List<CdrVisitNodeVO> visits;

    /** 归属不上任何就诊次的事件（单列出来，不静默丢弃） */
    @Schema(description = "未能归属到就诊次的事件")
    private List<CdrEventVO> unresolvedEvents;

    /** 健康档案分组（过敏史 / 既往史 / 手术史 / 家族史 / 用药史 / 联系人） */
    @Schema(description = "健康档案（过敏史 / 既往史 / 手术史 / 家族史 / 用药史 / 联系人）")
    private List<CdrProfileGroupVO> profile;

    /** 整体提示（如「本页含 2 份档案的数据」），页面顶部横幅用 */
    @Schema(description = "整体提示（如「本页含 2 份档案的数据」）")
    private List<String> warnings;
}
