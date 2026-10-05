package com.his.operation.entity;

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
 * 麻醉术前访视单（麻醉术前访视单）—— 一台手术一份（UNIQUE apply_id）。
 *
 * <p>这张表存在的唯一理由：<b>麻醉的风险必须在麻醉之前被发现</b>。
 * 困难气道、饱胃、抗凝药没停、ASA Ⅳ 级——这些都是在诱导之前才知道还能改方案的事，
 * 诱导之后才发现就是事故。所以这张单在链上的位置是"麻醉记录单的前置闸门"。
 *
 * <p>字段与表 <b>一一对应</b>（多一个库里没有的列 → 全表 select 直接 500）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_anesthesia_visit")
public class BizAnesthesiaVisit extends BaseEntity {

    /**
     * 访视单号（MF + yyyyMMdd + 4位序号）
     */
    private String visitNo;

    /**
     * 手术申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 手术申请单号（快照，列表不用 JOIN 也能显示）
     */
    private String applyNo;

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

    /**
     * 性别（快照）（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄（快照）
     */
    private Integer age;

    /**
     * 术前诊断（快照）
     */
    private String diagnosis;

    /**
     * 拟施手术编码（快照）
     */
    private String plannedOperationCode;

    /**
     * 拟施手术名称（快照）
     */
    private String plannedOperationName;

    /**
     * 手术级别（快照）1~4
     */
    private Integer operationLevel;

    /**
     * 拟施麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）
     */
    private Integer anesthesiaType;

    /**
     * 是否急诊手术（0-否 1-是）
     */
    private Integer isEmergency;

    /**
     * ASA 分级（1-Ⅰ 2-Ⅱ 3-Ⅲ 4-Ⅳ 5-Ⅴ）
     */
    private Integer asaGrade;

    /**
     * ASA E（急诊）标志：0-否 1-是
     */
    private Integer asaEmergency;

    /**
     * Mallampati 气道分级（1-Ⅰ 2-Ⅱ 3-Ⅲ 4-Ⅳ）
     */
    private Integer mallampati;

    /**
     * 张口度（cm）
     */
    private BigDecimal mouthOpenCm;

    /**
     * 颈部活动度（1-正常 2-受限 3-强直）
     */
    private Integer neckMobility;

    /**
     * 预计困难气道（0-否 1-是）
     */
    private Integer difficultAirway;

    /**
     * 气道评估补充说明
     */
    private String airwayNote;

    /**
     * 既往麻醉史与不良反应史
     */
    private String pastAnesthesiaHistory;

    /**
     * 过敏史（药物/食物/消毒剂）
     */
    private String allergyHistory;

    /**
     * 长期用药史（抗凝药/降压药/激素等必须写）
     */
    private String medicationHistory;

    /**
     * 吸烟饮酒史
     */
    private String smokeDrink;

    /**
     * 禁食禁饮（0-未禁食 1-已按要求禁食 2-急诊饱胃）
     */
    private Integer npoStatus;

    /**
     * 身高（cm）
     */
    private BigDecimal heightCm;

    /**
     * 体重（kg）
     */
    private BigDecimal weightKg;

    /**
     * 辅助检查摘要（血常规/凝血/ECG/胸片/电解质等）
     */
    private String examSummary;

    /**
     * 麻醉计划
     */
    private String anesthesiaPlan;

    /**
     * 监测计划（有创血压/CVP/BIS/体温等）
     */
    private String monitoringPlan;

    /**
     * 风险评估
     */
    private String riskAssessment;

    /**
     * 备选方案
     */
    private String backupPlan;

    /**
     * 访视结论：1-可施行麻醉 2-暂缓手术 3-需会诊/进一步评估
     */
    private Integer conclusion;

    /**
     * 结论说明
     */
    private String conclusionNote;

    /**
     * 访视状态（0-草稿 1-已完成）
     */
    private Integer visitStatus;

    /**
     * 访视麻醉医师ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long visitDoctorId;

    /**
     * 访视麻醉医师姓名（快照）
     */
    private String visitDoctorName;

    /**
     * 访视时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime visitTime;
}
