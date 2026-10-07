package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CDR 时间轴事件行（{@code CdrMapper#EVENT_SQL} 一行）。
 *
 * <p>对应那条 28 分支 UNION ALL 的统一行形状：每个分支只填自己有的列，
 * 时间列一律COALESCE 兜底（否则 etime 为 null，事件会排到时间轴最后，看着像"数据丢了"）。
 *
 * <p>为什么单独建行 VO 而不是直接复用 {@link CdrEventVO}：事件行是**数据库形状**
 * （含 srcTable/ownerPid 这类只服务追溯与EMPI 归并的列，且没翻译过码值）；
 * {@link CdrEventVO} 是**接口形状**（码值已翻译成文案、不再暴露来源表）。
 * 两者混用会让"库里存什么"和"页面显示什么"绑死在一起 —— 页面加一个展示字段就
 * 得改SQL 别名，SQL 改一个别名又会让页面静默少字段。
 */
@Data
public class CdrEventRowVO implements Serializable {

    /**
     * 事件类型码（outpatientRecord/prescription/laboratoryApply/... 见 CdrEventTypeEnum）
     */
    private String etype;

    /**
     * 来源表名（追溯用：这条事件是从哪张表来的）
     */
    private String srcTable;

    /**
     * 来源记录主键（字符串，避免前端丢精度）
     */
    private String srcId;

    /**
     * 锚点类型：REGIST-挂号 / ADMISSION-住院 / EMERGENCY-急诊 / PATIENT-患者级
     */
    private String anchorType;

    /**
     * 锚点ID（归属不到就诊次时为空，此事件会被单列到"未能归属"区）
     */
    private String anchorId;

    /**
     * 事件时间（各分支已 COALESCE 兜底，不为 null 除非源数据本身全空）
     */
    private LocalDateTime etime;

    /**
     * 标题
     */
    private String title;

    /**
     * 摘要（SQL 里已拼好"主诉 / 诊断 / 标本"这类短摘要）
     */
    private String summary;

    /**
     * 科室名称（部分分支无此列，为 null）
     */
    private String deptName;

    /**
     * 操作人/医生（部分分支无此列，为 null）
     */
    private String operatorName;

    /**
     * 状态码（各分支语义不同，翻译口径统一走 {@code CdrEventTypeEnum}）
     */
    private Integer statusCode;

    /**
     * 副码（文书类型/医嘱类别/诊断类型…，各分支语义不同）
     */
    private Integer secondaryCode;

    /**
     * 金额（可为null；语义标签由服务层按事件类型给，见 amountLabel）
     */
    private BigDecimal amount;

    /**
     * 数据归属档案ID（EMPI 归并后可能不是主档，故用字符串比对）
     */
    private String ownerPid;
}