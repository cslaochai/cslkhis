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

    /** 节点键（V-就诊次ID R-挂号ID A-入院ID E-急诊ID P-跨就诊轴） */
    @Schema(description = "节点键（V:就诊次ID / R:挂号ID / A:入院ID / E:急诊ID）")
    private String nodeKey;

    /** 节点类型码（OUTPATIENT-门诊 INPATIENT-住院 EMERGENCY-急诊 PATIENT-跨就诊） */
    @Schema(description = "节点类型码：OUTPATIENT/INPATIENT/EMERGENCY")
    private String nodeType;

    /** 节点类型中文名（由节点类型枚举 label 渲染） */
    @Schema(description = "节点类型中文名")
    private String nodeTypeText;

    /** 锚点ID（就诊次/入院/急诊的主键，跨就诊节点为患者ID） */
    @Schema(description = "锚点ID（就诊次/入院/急诊主键）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long anchorId;

    /** 锚点业务单号（挂号号 / 入院号 / 急诊号） */
    @Schema(description = "锚点业务号（挂号号 / 入院号 / 急诊号）")
    private String anchorNo;

    /** 节点标题（节点类型 + 科室 + 医师拼出，如「门诊 · 消化内科 · 张三」） */
    @Schema(description = "节点标题，如「门诊 · 消化内科 · 张三」")
    private String title;

    /** 节点副标题（住院显示病区床位，急诊显示分诊级别分区，跨就诊说明事件性质） */
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

    /** 接诊医生姓名 */
    @Schema(description = "医生")
    private String operatorName;

    /** 状态文本 */
    @Schema(description = "节点状态文案（就诊状态 / 在院状态 / 急诊状态）")
    private String statusText;

    /** 住院天数（仅住院节点有值，按入院到出院的自然日算，不足一天按一天） */
    @Schema(description = "住院天数（仅住院节点）")
    private Integer durationDays;

    /** 合计金额 */
    @Schema(description = "本次费用合计")
    private BigDecimal totalAmount;

    /** 转归/诊断摘要（住院取出院诊断、急诊取初步诊断，文本而非码值） */
    @Schema(description = "结局/诊断摘要")
    private String outcome;

    /** 归到本节点的事件总数 */
    @Schema(description = "本节点事件总数")
    private Integer eventCount;

    /** 本节点按事件类型汇总的条数 */
    @Schema(description = "本节点各类事件计数")
    private List<CdrCountVO> counts;

    /** 病历完整性缺口提示（空=该节点该有的东西都在，如"有处方但没有门诊病历"） */
    @Schema(description = "数据缺口提示（空=该节点该有的东西都在）")
    private List<String> gaps;

    /** 本节点事件明细（按时间倒序） */
    @Schema(description = "本节点事件（按时间倒序）")
    private List<CdrEventVO> events;

    /** 本节点数据是否来自被并档的影子档案（EMPI 归并后非主档贡献） */
    @Schema(description = "本节点数据是否来自被并档（EMPI 影子档案）")
    private Boolean fromShadow;
}
