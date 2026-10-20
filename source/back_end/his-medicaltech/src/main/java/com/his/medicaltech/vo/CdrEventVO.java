package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * CDR 时间轴上的一个**事件**（挂号、处方、检验申请、病程、手术、结算……）
 */
@Data
@Schema(description = "CDR 事件")
public class CdrEventVO {

    /** 事件类型码 */
    @Schema(description = "事件类型码")
    private String eventType;

    /** 事件类型中文名（由事件类型枚举 label 渲染） */
    @Schema(description = "事件类型中文名")
    private String eventTypeText;

    /** 数据来源标识（用于追溯这条事件取自哪张业务单据，页面显示为「数据来源」） */
    @Schema(description = "来源表（用于追溯，页面上可显示为「数据来源」）")
    private String sourceTable;

    /** 来源单据ID */
    @Schema(description = "来源记录主键（字符串，避免前端丢失精度）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;

    /** 事件时间 yyyy-MM-dd HH:mm:ss */
    @Schema(description = "事件时间 yyyy-MM-dd HH:mm:ss")
    private String eventTime;

    /** 事件标题（如诊断名、手术名称、文书题名） */
    @Schema(description = "标题")
    private String title;

    /** 小结 */
    @Schema(description = "摘要")
    private String summary;

    /** 科室名称 */
    @Schema(description = "科室")
    private String deptName;

    /** 操作人姓名 */
    @Schema(description = "操作人/医生")
    private String operatorName;

    /** 状态原始码（宿主枚举随 eventType 变化，如处方走处方状态、检查报告走检查记录状态） */
    @Schema(description = "状态码（原始值）")
    private Integer statusCode;

    /** 状态文本 */
    @Schema(description = "状态文案（服务端翻译，未知码值返回空串）")
    private String statusText;

    /** 副码原始码值（随 eventType 变化：文书类型/医嘱类型/诊断类型/处方类型…） */
    @Schema(description = "副码（文书类型/医嘱类别/诊断类型…）")
    private Integer secondaryCode;

    /** 副码文案（由枚举 label 渲染） */
    @Schema(description = "副码文案")
    private String secondaryText;

    /** 副码的中文标签（如「文书类型」）；为空表示这类事件没有副码 */
    @Schema(description = "副码中文标签（如「文书类型」）")
    private String secondaryLabel;

    /** 金额（仅收费/结算/预交金类事件有值，其余为空） */
    @Schema(description = "金额（可空）")
    private BigDecimal amount;

    /** 金额的业务口径名称（按事件类型给出，如「金额」「实收」） */
    @Schema(description = "金额标签（如「金额」「实收」）")
    private String amountLabel;

    /** 所属就诊次锚点类型（REGIST-挂号 ADMISSION-入院 EMERGENCY-急诊 PATIENT-跨就诊） */
    @Schema(description = "所属就诊次锚点类型：REGIST/ADMISSION/PATIENT")
    private String anchorType;

    /** 所属就诊次锚点ID（为空=未能归属到就诊次） */
    @Schema(description = "所属就诊次锚点ID（为空=未能归属到就诊次）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long anchorId;

    /** 数据归属档案ID（EMPI 归并后可能不是主档） */
    @Schema(description = "数据归属档案ID（EMPI 归并后可能不是主档）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ownerPatientId;

    /** 归属档案的单号（影子档案的数据一眼可辨；主档自身不填） */
    @Schema(description = "数据归属档案的住院/挂号单号（影子档案的数据一眼可辨）")
    private String ownerArchiveNo;
}
