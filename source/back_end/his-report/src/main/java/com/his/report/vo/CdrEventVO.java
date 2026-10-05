package com.his.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * CDR 时间轴上的一个**事件**（挂号、处方、检验申请、病程、手术、结算……）
 *
 * <p>设计口径：
 * <ul>
 *   <li>{@code eventTime} 是**字符串**（{@code yyyy-MM-dd HH:mm:ss}），不是 LocalDateTime。
 *       时间轴要能被前端直接排序/展示，不做二次格式化比什么都稳。</li>
 *   <li>状态、副码（文书类型/医嘱类别…）都在服务端翻译好，前端只负责显示
 *       —— 码值口径不允许前端各写一份。</li>
 *   <li>{@code anchorId} 为空表示这条事件**没能归属到任何就诊次**（如报告单找不到对应申请）。
 *       这种事件不会被丢掉，会挂在"未能归属"区里显式暴露出来。</li>
 * </ul>
 */
@Data
@Schema(description = "CDR 事件")
public class CdrEventVO {

    @Schema(description = "事件类型码")
    private String eventType;

    @Schema(description = "事件类型中文名")
    private String eventTypeText;

    @Schema(description = "来源表（用于追溯，页面上可显示为「数据来源」）")
    private String sourceTable;

    /** 来源单据ID */
    @Schema(description = "来源记录主键（字符串，避免前端丢失精度）")
    private String sourceId;

    @Schema(description = "事件时间 yyyy-MM-dd HH:mm:ss")
    private String eventTime;

    @Schema(description = "标题")
    private String title;

    /** 小结 */
    @Schema(description = "摘要")
    private String summary;

    /** 科室名称 */
    @Schema(description = "科室")
    private String deptName;

    @Schema(description = "操作人/医生")
    private String operatorName;

    @Schema(description = "状态码（原始值）")
    private Integer statusCode;

    /** 状态文本 */
    @Schema(description = "状态文案（服务端翻译，未知码值返回空串）")
    private String statusText;

    @Schema(description = "副码（文书类型/医嘱类别/诊断类型…）")
    private Integer secondaryCode;

    @Schema(description = "副码文案")
    private String secondaryText;

    @Schema(description = "副码中文标签（如「文书类型」）")
    private String secondaryLabel;

    @Schema(description = "金额（可空）")
    private BigDecimal amount;

    @Schema(description = "金额标签（如「金额」「实收」）")
    private String amountLabel;

    @Schema(description = "所属就诊次锚点类型：REGIST/ADMISSION/PATIENT")
    private String anchorType;

    @Schema(description = "所属就诊次锚点ID（为空=未能归属到就诊次）")
    private String anchorId;

    @Schema(description = "数据归属档案ID（EMPI 归并后可能不是主档）")
    private String ownerPatientId;

    @Schema(description = "数据归属档案的住院/挂号单号（影子档案的数据一眼可辨）")
    private String ownerArchiveNo;
}
