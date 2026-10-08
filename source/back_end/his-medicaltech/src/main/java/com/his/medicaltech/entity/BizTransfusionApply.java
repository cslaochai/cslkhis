package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院输血申请单—— 输血闭环的主单。
 *
 * <p>字段与表 <b>一一对应</b>（多一个库里没有的列 → 全表 select 直接 500）。
 *
 * <p>这条链要回答的问题，按发生顺序：
 * <ol>
 *   <li>谁、什么时候、为什么输 → {@code applyDoctorId / applyTime / indication / transfusionPurpose}；</li>
 *   <li>受血者是什么血型（本次鉴定结果）→ {@code patientAbo / patientRh}；</li>
 *   <li>配了哪几袋、相不相合 → {@code crossmatchStatus} + 子表输血血袋明细；</li>
 *   <li>谁发的血 → {@code issueDoctorId / issueTime}；</li>
 *   <li>谁和谁双人核对、核对项是什么、什么时候开始输什么时候输完 → {@code checkNurseId / checkNurse2Id / checkItems / infusionStartTime / infusionEndTime}；</li>
 *   <li>有没有反应、怎么处理的 → {@code hasReaction / reactionType / reactionHandle}；</li>
 *   <li>结果落到哪份正式文书上 → {@code recordId}（record_type=11 输血记录）。</li>
 * </ol>
 *
 * <p><b>{@code patientAbo} / {@code patientRh} 是 NOT NULL</b>：没有受血者血型，
 * 后面所有的"配血相合"都是空话。Rh 必须单独记 —— 患者档案的
 * 患者基本信息.blood_type 只有 A/B/O/AB，**没有 Rh 维度**，
 * 而 Rh 阴性是稀有血型、直接影响备血方案。
 *
 * <p>{@code crossmatchStatus} 与 {@code transfusionStatus} <b>不是一回事</b>，不能合并：
 * 前者回答"血配好了没、合不合"，后者回答"流程走到哪一步"。
 * 配血不合时流程状态会停在 0（待配血），但 {@code crossmatchStatus=3} 必须能看见 ——
 * 否则"配了、但不合"会被显示成"还没配"，把已经发生的安全隐患抹掉。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_transfusion_apply")
public class BizTransfusionApply extends BaseEntity {

    /**
     * 输血申请单号（SX + yyyyMMdd + 4位序号）
     */
    private String applyNo;

    /**
     * 入院ID（入院记录的入院ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 入院号
     */
    private String admissionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    // 申请方

    /**
     * 申请科室ID（= 患者当前科室，服务端推导）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDeptId;

    /**
     * 申请科室名称
     */
    private String applyDeptName;

    /**
     * 申请时所在病区名称
     */
    private String applyWardName;

    /**
     * 申请时床号
     */
    private String applyBedNo;

    /**
     * 申请医生ID（员工ID，不是用户的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /**
     * 申请医生姓名
     */
    private String applyDoctorName;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    // 受血者血型

    /**
     * 受血者ABO血型：A/B/O/AB
     */
    private String patientAbo;

    /**
     * 受血者Rh血型：阳/阴
     */
    private String patientRh;

    // 用血需求

    /**
     * 血液品种（1-红细胞悬液 2-血浆 3-血小板 4-冷沉淀 5-全血 6-其他）
     */
    private Integer bloodComponent;

    /**
     * 规格（如 1.5U / 200ml / 1治疗量）
     */
    private String componentSpec;

    /**
     * 申请袋数（配血累计不得超过此数）
     */
    private Integer bagCount;

    /**
     * 申请总量
     */
    private BigDecimal plannedAmount;

    /**
     * 总量单位：U / ml / 治疗量
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
     * 输血前血小板 PLT（×10^9/L）
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
     * 妊娠史（育龄女性）
     */
    private String pregnancyHistory;

    /**
     * 是否紧急用血（0-否 1-是）
     */
    private Integer isEmergency;

    // 配血

    /**
     * 配血状态：0-待配血 1-配血中（未配齐）2-全部相合且配齐 3-存在配血不合
     */
    private Integer crossmatchStatus;

    /**
     * 配血人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long crossmatchDoctorId;

    /**
     * 配血人姓名
     */
    private String crossmatchDoctorName;

    /**
     * 配血完成时间（全部相合的时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime crossmatchTime;

    // 发血

    /**
     * 发血人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long issueDoctorId;

    /**
     * 发血人姓名
     */
    private String issueDoctorName;

    /**
     * 发血时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issueTime;

    // 输注（双人核对）

    /**
     * 输血前核对要点码（逗号分隔，如 1,2,3,4,5,6）
     */
    private String checkItems;

    /**
     * 核对补充说明（异常项必须写在这里）
     */
    private String checkNote;

    /**
     * 核对护士1 ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long checkNurseId;

    /**
     * 核对护士1 姓名
     */
    private String checkNurseName;

    /**
     * 核对护士2 ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long checkNurse2Id;

    /**
     * 核对护士2 姓名
     */
    private String checkNurse2Name;

    /**
     * 双人核对时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;

    /**
     * 输注执行护士ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long infusionNurseId;

    /**
     * 输注执行护士姓名
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
     * 滴速（如 60滴/分）
     */
    private String infusionSpeed;

    /**
     * 输注过程观察（生命体征与不良反应）
     */
    private String observation;

    // 输血反应上报

    /**
     * 有无输血反应（0-未上报 1-已上报有反应）
     */
    private Integer hasReaction;

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
     * 上报人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reactionReporterId;

    /**
     * 上报人姓名
     */
    private String reactionReporterName;

    /**
     * 上报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reactionTime;

    // 疗效评估 / 完成

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
     * 输血后血小板 PLT（×10^9/L）
     */
    private Integer postPlt;

    /**
     * 完成录入人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long finishDoctorId;

    /**
     * 完成录入人姓名
     */
    private String finishDoctorName;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /**
     * 回写住院病历ID（住院病历文书的ID，record_type=11 输血记录）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    // 状态

    /**
     * 状态（0-待配血 1-已配血 2-已发血 3-输注中 4-已完成 5-已取消）
     */
    private Integer transfusionStatus;

    /**
     * 取消原因（仅待配血/已配血/已发血可取消）
     */
    private String cancelReason;

    /**
     * 取消人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long cancelDoctorId;

    /**
     * 取消人姓名
     */
    private String cancelDoctorName;

    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    // 用血分级审批（sql/93；《医疗机构临床用血管理办法》分级审核签发）

    /**
     * 申请量折算毫升数（ml直取；1U≈200ml；1治疗量≈250ml；无法折算按最高级审批）
     */
    private Integer amountMl;

    /**
     * 审批级别（服务端按折算量推导）：1-上级医师（主治及以上，<400ml）2-科主任（400~799ml）3-医务科（≥800ml）
     */
    private Integer approveLevel;

    /**
     * 审批状态（0-待审批 1-已通过 2-已驳回 3-急诊待补审）
     */
    private Integer approveStatus;

    /**
     * 最近一次驳回原因（驳回后修改重提即清空）
     */
    private String approveRejectReason;

    /**
     * 审批通过时间（补审=补办时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    /**
     * 是否急诊补审（0-常规审批 1-急诊后补）
     */
    private Integer approveMakeup;
}
