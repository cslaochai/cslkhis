package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.his.emr.entity.BizPrescriptionDetail;

/**
 * 中药代煎服务单（sql/139）。
 *
 * <p>定位是<b>服务台账</b>，不是第二份收费单据：药已发出去才生成，
 * 一张处方最多一张（{@code uk_prescription}），所以本表**没有删除路径**，
 * 只能作废（status=9）并留原因。
 *
 * <p>患者/剂量等字段全是<b>快照</b>：煎药室手上那张回执要能独立成立，
 * 不能因为处方后来被改、患者被合并就看不出这单煎的是谁。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_tcm_decoct")
public class BizTcmDecoct extends BaseEntity {

    /**
     * 代煎单号（TCMD + yyyyMMdd + 4 位序号，服务端现算）
     */
    private String decoctNo;

    /**
     * 处方ID（处方主表的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 处方号（快照）
     */
    private String prescriptionNo;

    /** 患者ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    private String patientName;

    /**
     * 开方科室（快照）
     */
    private String deptName;

    /**
     * 开方医师（快照）
     */
    private String doctorName;

    /**
     * 剂数（来自处方头：煎几付、发几袋）
     */
    private Integer doseCount;

    /**
     * 味数（处方明细条数快照）
     */
    private Integer herbCount;

    /**
     * 全方总克数（各明细实发克数之和）
     */
    private BigDecimal totalGrams;

    /**
     * 煎法脚注汇总（如「先煎：牡蛎；后下：大黄」，由明细 route 现算后存快照）
     */
    private String methodSummary;

    /**
     * 状态（1-待煎 2-已煎 3-已取 9-已作废，TcmDecoctStatusEnum）
     */
    private Integer decoctStatus;

    /** 代煎药房ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pharmacyId;

    /** 代煎药房名称（快照） */
    private String pharmacyName;

    /** 最近一次状态操作人 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /** 最近一次状态操作人姓名（快照） */
    private String operatorName;

    /** 煎药完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime decoctTime;

    /** 患者取走时间（终态） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickupTime;

    /**
     * 作废原因（status=9 必填，写入前先截到列宽 200）
     */
    private String cancelReason;

    /**
     * 处方明细（不落本表列，详情出参时按 prescriptionId 现查）
     */
    @TableField(exist = false)
    private java.util.List<BizPrescriptionDetail> details;
}
