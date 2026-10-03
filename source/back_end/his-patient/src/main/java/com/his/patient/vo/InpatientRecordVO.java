package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院病历文书列表行 VO。
 *
 * <p>刻意**不带大 TEXT 字段**（现病史/体格检查正文等只在详情里给）：
 * 列表里放一堆长文本会让前端渲染慢、也让"列表看着有值、其实没查"这种误会没有发生的余地。
 *
 * <p>按钮可用性（{@code canEdit/canSubmit/canArchive}）一律由后端给，前端不自己判状态码
 * —— 与 P1 医嘱工作区同一口径，避免第二套状态机。
 */
@Data
public class InpatientRecordVO implements Serializable {

    /**
     * 文书ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 病历文书号 */
    private String recordNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /** 病区名称（快照） */
    private String wardName;

    /** 床号（快照） */
    private String bedNo;

    /**
     * 文书类型
     */
    private Integer recordType;

    /**
     * 文书类型文案
     */
    private String recordTypeText;

    /**
     * 文书标题
     */
    private String recordTitle;

    /**
     * 记录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;

    /** 文书状态（1-草稿 2-已提交 3-已归档） */
    private Integer recordStatus;

    /**
     * 文书状态文案
     */
    private String recordStatusText;

    /**
     * 书写医生姓名
     */
    private String doctorName;

    /**
     * 主诉（列表里给一眼可辨的摘要）
     */
    private String chiefComplaint;

    /**
     * 诊断名称
     */
    private String diagnosisName;

    // 结构化率（口径见 RecordStructuredFields）

    /**
     * 已填结构化要素数
     */
    private Integer structuredFilled;

    /**
     * 该文书类型应填的结构化要素数（分母）
     */
    private Integer structuredTotal;

    /**
     * 结构化率（百分数，2 位小数）
     */
    private BigDecimal structuredRate;

    /**
     * 结构化率文案（如 "81.48%"；分母为 0 时为 "—"，不伪装成 0%）
     */
    private String structuredRateText;

    /**
     * 缺失要素中文名（定位到"缺哪一项"）
     */
    private List<String> missingLabels;

    // 按钮可用性（后端判定）

    /**
     * 是否可编辑（草稿/已提交可改，已归档不可）
     */
    @Schema(description = "是否可编辑：已归档为 false")
    private Boolean canEdit;

    /**
     * 是否可提交（草稿可提交）
     */
    private Boolean canSubmit;

    /**
     * 是否可归档（已提交可归档）
     */
    private Boolean canArchive;

    /**
     * 归档时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime archiveTime;

    /**
     * 归档人姓名
     */
    private String archiveByName;

    /**
     * 签名状态（0-未签名 1-已签名 2-签名已失效）
     */
    private Integer signStatus;

    /**
     * 签名状态文案
     */
    private String signStatusText;

    /**
     * 当前有效签名ID（可跳转签名中心查看证据）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long signId;

    /**
     * 最近一次签名时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signedTime;
}
