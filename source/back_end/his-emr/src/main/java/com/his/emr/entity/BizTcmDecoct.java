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
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 中药代煎服务单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_tcm_decoct")
public class BizTcmDecoct extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



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
     * 处方号
     */
    private String prescriptionNo;

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
     * 开方科室
     */
    private String deptName;

    /**
     * 开方医师
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

    /**
     * 代煎药房ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pharmacyId;

    /**
     * 代煎药房名称
     */
    private String pharmacyName;

    /**
     * 最近一次状态操作人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 最近一次状态操作人姓名
     */
    private String operatorName;

    /**
     * 煎药完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime decoctTime;

    /**
     * 患者取走时间（终态）
     */
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
