package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * CDR 时间轴的**一个就诊次节点**：一次门诊 / 一次住院 / 一次急诊。
 */
@Data
@Schema(description = "CDR 就诊次节点")
public class CdrVisitNodeVO {

    @Schema(description = "节点键（V:就诊次ID / R:挂号ID / A:入院ID / E:急诊ID）")
    private String nodeKey;

    @Schema(description = "节点类型码：OUTPATIENT/INPATIENT/EMERGENCY")
    private String nodeType;

    @Schema(description = "节点类型中文名")
    private String nodeTypeText;

    @Schema(description = "锚点ID（就诊次/入院/急诊主键）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long anchorId;

    @Schema(description = "锚点业务号（挂号号 / 入院号 / 急诊号）")
    private String anchorNo;

    @Schema(description = "节点标题，如「门诊 · 消化内科 · 张三」")
    private String title;

    @Schema(description = "节点副标题（住院显示病区床位；门诊显示挂号类型）")
    private String subtitle;

    /** 开始时间 */
    @Schema(description = "开始时间 yyyy-MM-dd HH:mm:ss")
    private String startTime;

    /** 结束时间 */
    @Schema(description = "结束时间（可空）")
    private String endTime;

    /** 科室名称 */
    @Schema(description = "科室")
    private String deptName;

    @Schema(description = "医生")
    private String operatorName;

    /** 状态文本 */
    @Schema(description = "节点状态文案（就诊状态 / 在院状态 / 急诊状态）")
    private String statusText;

    @Schema(description = "住院天数（仅住院节点）")
    private Integer durationDays;

    /** 合计金额 */
    @Schema(description = "本次费用合计")
    private BigDecimal totalAmount;

    @Schema(description = "结局/诊断摘要")
    private String outcome;

    @Schema(description = "本节点事件总数")
    private Integer eventCount;

    @Schema(description = "本节点各类事件计数")
    private List<CdrCountVO> counts;

    @Schema(description = "数据缺口提示（空=该节点该有的东西都在）")
    private List<String> gaps;

    @Schema(description = "本节点事件（按时间倒序）")
    private List<CdrEventVO> events;

    @Schema(description = "本节点数据是否来自被并档（EMPI 影子档案）")
    private Boolean fromShadow;
}
