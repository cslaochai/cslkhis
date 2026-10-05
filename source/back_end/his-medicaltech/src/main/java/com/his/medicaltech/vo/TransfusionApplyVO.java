package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院输血申请出参。
 *
 * <p>三条约定（与会诊/转科/手术 VO 一致）：
 * <ol>
 *   <li>所有 ID 走 {@code ToStringSerializer}：雪花 ID 超 JS 精度，截断后会变成"记录不存在"的假象。</li>
 *   <li>码值一律带 {@code xxxText} 文案，且由后端给 —— 前端不自己判状态、不自己拼中文。</li>
 *   <li>{@code canXxx} 由后端按状态算好：按钮可用性属于业务规则，不属于前端。</li>
 * </ol>
 *
 * <p>特别注意 {@code crossmatchStatusText} 与 {@code transfusionStatusText} 是<b>两个</b>状态：
 * 前者是"血配好没、合不合"，后者是"流程走到哪一步"。配血不合时流程停在「待配血」，
 * 但配血状态必须显示「存在配血不合」—— 否则已经发生的安全隐患被显示成"还没开始"。
 */
@Data
public class TransfusionApplyVO implements Serializable {

    /**
     * 输血申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 输血申请单号
     */
    private String applyNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 入院号（快照）
     */
    private String admissionNo;

    /**
     * 入院状态（1-在院 0-已出院）—— 已出院的输血单不允许再配血/发血/完成
     */
    private Integer admitStatus;

    /**
     * 入院状态文案
     */
    private String admitStatusText;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号（快照）
     */
    private String patientNo;

    /**
     * 患者姓名（申请时快照）
     */
    private String patientName;

    /**
     * 性别（快照）（1-男 2-女）
     */
    private Integer gender;

    /**
     * 性别文案
     */
    private String genderText;

    /**
     * 年龄（快照）
     */
    private Integer age;

    // 申请方

    /**
     * 申请科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDeptId;

    /**
     * 申请科室名称（快照）
     */
    private String applyDeptName;

    /**
     * 申请时所在病区名称（快照）
     */
    private String applyWardName;

    /**
     * 申请时床号（快照）
     */
    private String applyBedNo;

    /**
     * 申请医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /**
     * 申请医生姓名（快照）
     */
    private String applyDoctorName;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    // 受血者血型

    /**
     * 受血者 ABO 血型
     */
    private String patientAbo;

    /**
     * 受血者 Rh 血型
     */
    private String patientRh;

    /**
     * 受血者血型文案（如「A 型 Rh(+)」）
     */
    private String bloodTypeText;

    /**
     * 与患者档案血型是否一致（不一致时不阻断，但必须能看见；档案常是旧的/未查的）
     */
    private Boolean aboMatchesArchive;

    // 用血需求

    /**
     * 血液品种（1-红细胞悬液 2-血浆 3-血小板 4-冷沉淀 5-全血 6-其他）
     */
    private Integer bloodComponent;

    /**
     * 血液品种文案
     */
    private String bloodComponentText;

    /**
     * 规格
     */
    private String componentSpec;

    /**
     * 申请袋数
     */
    private Integer bagCount;

    /**
     * 已配血袋数（展示"已配 1/2 袋"）
     */
    private Integer matchedBagCount;

    /**
     * 配血进度文案（如「已配 1/2 袋」「全部相合 2/2」）
     */
    private String bagProgressText;

    /**
     * 申请总量
     */
    private BigDecimal plannedAmount;

    /**
     * 总量单位
     */
    private String amountUnit;

    /**
     * 输血目的（纠正贫血/补充凝血因子/提升血小板…）
     */
    private String transfusionPurpose;

    /**
     * 输血指征（Hb/HCT/PLT 指标 + 临床症状，缺了就是无指征用血）
     */
    private String indication;

    /**
     * 输血前血红蛋白 Hb（g/L）
     */
    private BigDecimal preHb;

    /**
     * 输血前红细胞压积 HCT（%）
     */
    private BigDecimal preHct;

    /**
     * 输血前 PLT（×10^9/L）
     */
    private Integer prePlt;

    /**
     * 既往输血史
     */
    private String transfusionHistory;

    /**
     * 既往输血反应史
     */
    private String reactionHistory;

    /**
     * 妊娠史
     */
    private String pregnancyHistory;

    /**
     * 是否紧急用血（0-否 1-是）
     */
    private Integer isEmergency;

    /**
     * 紧急用血文案
     */
    private String isEmergencyText;

    // 配血

    /**
     * 配血状态：0-待配血 1-配血中 2-全部相合 3-存在不合
     */
    private Integer crossmatchStatus;

    /**
     * 配血状态文案
     */
    private String crossmatchStatusText;

    /**
     * 配血人姓名（快照）
     */
    private String crossmatchDoctorName;

    /**
     * 配血完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime crossmatchTime;

    // 发血

    /**
     * 发血人姓名（快照）
     */
    private String issueDoctorName;

    /**
     * 发血时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issueTime;

    // 输注

    /**
     * 输血前核对要点码
     */
    private String checkItems;

    /**
     * 核对要点文案（列表展示用）
     */
    private String checkItemsText;

    /**
     * 核对补充说明
     */
    private String checkNote;

    /**
     * 核对护士1 姓名（快照）
     */
    private String checkNurseName;

    /**
     * 核对护士2 姓名（快照）
     */
    private String checkNurse2Name;

    /**
     * 双人核对时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;

    /**
     * 输注执行护士姓名（快照）
     */
    private String infusionNurseName;

    /**
     * 输注开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime infusionStartTime;

    /**
     * 输注结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime infusionEndTime;

    /**
     * 实际输注量
     */
    private BigDecimal actualAmount;

    /**
     * 滴速
     */
    private String infusionSpeed;

    /**
     * 输注过程观察
     */
    private String observation;

    // 输血反应

    /**
     * 有无输血反应（0-未上报 1-已上报有反应）
     */
    private Integer hasReaction;

    /**
     * 反应文案
     */
    private String hasReactionText;

    /**
     * 反应类型
     */
    private String reactionType;

    /**
     * 反应描述
     */
    private String reactionDesc;

    /**
     * 处理措施
     */
    private String reactionHandle;

    /**
     * 上报人姓名（快照）
     */
    private String reactionReporterName;

    /**
     * 上报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reactionTime;

    // 疗效评估 / 回写

    /**
     * 输注后疗效评估
     */
    private String efficacyEval;

    /**
     * 输血后血红蛋白 Hb（g/L）
     */
    private BigDecimal postHb;

    /**
     * 输血后红细胞压积 HCT（%）
     */
    private BigDecimal postHct;

    /**
     * 输血后 PLT（×10^9/L）
     */
    private Integer postPlt;

    /**
     * 完成录入人姓名（快照）
     */
    private String finishDoctorName;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /**
     * 回写的住院病历ID（record_type=11 输血记录）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 回写的病历号（病历里查得到这次输血的证据）
     */
    private String recordNo;

    // 状态

    /**
     * 状态（0-待配血 1-已配血 2-已发血 3-输注中 4-已完成 5-已取消）
     */
    private Integer transfusionStatus;

    /**
     * 状态文案
     */
    private String transfusionStatusText;

    /**
     * 取消原因（仅待配血/已配血/已发血可取消）
     */
    private String cancelReason;

    /**
     * 取消人姓名（快照）
     */
    private String cancelDoctorName;

    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /**
     * 备注
     */
    private String remark;

    // 以下为服务层算出的展示 / 可用性字段

    /**
     * 输注时长（分钟）：结束 - 开始
     */
    private Long durationMinutes;

    /**
     * 输注时长文案
     */
    private String durationText;

    /**
     * 等待/流转耗时文案
     */
    private String waitText;

    /**
     * 该单是否已卡住（如"已配血超过 24 小时仍未发血"）—— 查询时算，不落状态列
     */
    private Boolean stalled;

    /**
     * 卡住提示文案
     */
    private String stalledText;

    /**
     * 可否修改申请（仅待配血）
     */
    private Boolean canEdit;

    /**
     * 可否配血（待配血）
     */
    private Boolean canCrossmatch;

    /**
     * 可否发血（已配血且全部相合）
     */
    private Boolean canIssue;

    /**
     * 可否开始输注（已发血）
     */
    private Boolean canStart;

    /**
     * 可否登记完成（输注中）
     */
    private Boolean canFinish;

    /**
     * 可否取消（待配血 / 已配血 / 已发血）
     */
    private Boolean canCancel;

    /**
     * 可否上报输血反应（已完成且尚未上报）
     */
    private Boolean canReportReaction;

    /**
     * 血袋明细（按配血顺序；列表接口不回，详情接口回）
     */
    private List<TransfusionBagVO> bags;

    /**
     * 输血前核对要点字典（前端渲染勾选框用）
     */
    private List<CheckItem> checkItemOptions;

    /**
     * 可选的反应类型（受控字典：不接受自由文本，否则统计永远凑不到一起）
     */
    private List<String> reactionTypeOptions;

    // 用血分级审批（sql/93）

    /**
     * 申请量折算毫升数
     */
    private Integer amountMl;

    /**
     * 审批级别：1-上级医师 2-科主任 3-医务科
     */
    private Integer approveLevel;

    /**
     * 审批状态（0-待审批 1-已通过 2-已驳回 3-急诊待补审）
     */
    private Integer approveStatus;

    /**
     * 最近一次驳回原因
     */
    private String approveRejectReason;

    /**
     * 审批通过时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    /**
     * 是否急诊补审（0-常规审批 1-急诊后补）
     */
    private Integer approveMakeup;

    /**
     * 审批状态文案
     */
    private String approveStatusText;

    /**
     * 审批级别文案
     */
    private String approveLevelText;

    /**
     * 审批流水（详情接口回，列表不回）
     */
    private List<ApproveRecord> approveRecords;

    /**
     * 是否可审批（decorate 派生：待配血且状态为待审批/急诊待补审）
     */
    private Boolean canApprove;

    /**
     * 审批记录行
     */
    @Data
    public static class ApproveRecord implements Serializable {
        /**
         * 输血申请单ID
         */
        private Long id;
        /**
         * 审批级别（1-上级医师 400-799ml）
         */
        private Integer approveLevel;
        /**
         * 审批级别文案
         */
        private String approveLevelText;
        /**
         * 结论：1-通过 2-驳回
         */
        private Integer approveResult;
        /**
         * 结论文案
         */
        private String approveResultText;
        /**
         * 审批人ID
         */
        private Long approverId;
        /**
         * 审批人姓名
         */
        private String approverName;
        /**
         * 审批人职称（快照）
         */
        private String approverTitle;
        /**
         * 审批意见
         */
        private String opinion;
        /**
         * 是否急诊补审
         */
        private Integer isMakeup;
        /**
         * 审批通过时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime approveTime;
    }

    /**
     * 审批统计
     */
    @Data
    public static class ApproveStats implements Serializable {
        /**
         * 待审批数
         */
        private long pending;
        /**
         * 已通过数
         */
        private long approved;
        /**
         * 已驳回数
         */
        private long rejected;
        /**
         * 急诊待补审数
         */
        private long makeupPending;
        /**
         * 分级计数：level-级别 value-数量
         */
        private List<LevelCount> byLevel;
    }

    /**
     * 分级计数行
     */
    @Data
    public static class LevelCount implements Serializable {
        /**
         * 审批级别（1-上级医师 400-799ml）
         */
        private Integer approveLevel;
        /**
         * 级别文案
         */
        private String approveLevelText;
        /**
         * 数量
         */
        private long count;
    }

    /**
     * 核对要点
     */
    @Data
    public static class CheckItem implements Serializable {
        /**
         * 码值
         */
        private Integer code;
        /**
         * 文案
         */
        private String label;
        /**
         * 是否必核项
         */
        private Boolean required;
    }
}
