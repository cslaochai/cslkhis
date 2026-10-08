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
 * PACU 麻醉后监测治疗记录（PACU 复苏记录）—— 一次麻醉一段停留。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_anesthesia_pacu")
public class BizAnesthesiaPacu extends BaseEntity {

    /**
     * 复苏单号（FS + yyyyMMdd + 4位序号）
     */
    private String pacuNo;

    /**
     * 麻醉记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 麻醉记录单号
     */
    private String recordNo;

    /**
     * 手术申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

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

    /**
     * 入 PACU 时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enterTime;

    /**
     * 出 PACU 时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leaveTime;

    /**
     * 复苏护士ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseId;

    /**
     * 复苏护士姓名
     */
    private String nurseName;

    /**
     * 负责麻醉医师ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long anesthetistId;

    /**
     * 负责麻醉医师姓名
     */
    private String anesthetistName;

    /**
     * Aldrete 肌力/活动：0-无 1-两肢可动 2-四肢可动
     */
    private Integer scoreActivity;

    /**
     * Aldrete 呼吸：0-需辅助通气 1-呼吸浅/受限 2-深呼吸可咳嗽
     */
    private Integer scoreRespiration;

    /**
     * Aldrete 血压：0-±50mmHg以上波动 1-±20~50 2-±20 以内
     */
    private Integer scoreCirculation;

    /**
     * Aldrete 意识：0-无反应 1-可唤醒 2-完全清醒
     */
    private Integer scoreConsciousness;

    /**
     * Aldrete 氧合：0-吸氧下<90% 1-吸氧下>90% 2-空气下>92%
     */
    private Integer scoreSpo2;

    /**
     * Aldrete 总分（服务端逐项相加，满分 10；出室标准 ≥9）
     */
    private Integer aldreteTotal;

    /**
     * 清醒程度（1-完全清醒 2-嗜睡可唤醒 3-未清醒）
     */
    private Integer awareness;

    /**
     * 是否发生并发症（0-无 1-有）
     */
    private Integer complicationFlag;

    /**
     * 并发症经过与处理
     */
    private String complicationNote;

    /**
     * 氧疗方式
     */
    private String oxygenTherapy;

    /**
     * 镇痛方式
     */
    private String analgesia;

    /**
     * 出室去向（1-回病房 2-转ICU 3-继续留观）
     */
    private Integer disposition;

    /**
     * 是否满足出室标准（Aldrete ≥ 9）：0-否 1-是
     */
    private Integer leaveCriteriaMet;

    /**
     * 状态（0-在室 1-已出室）
     */
    private Integer status;

    /**
     * 计费状态（0-未计费 1-已计费 2-计费失败）
     */
    private Integer chargeStatus;

    /**
     * 记账单号（费用记账流水的费用编号，本次计费最后一笔）
     */
    private String feeNo;

    /**
     * 本次计入金额（元）
     */
    private BigDecimal chargedAmount;

    /**
     * 计费失败原因
     */
    private String chargeFailReason;
}
