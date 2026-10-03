package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 处方点评明细 VO（含处方药品明细文本，点评弹框直接渲染） */
@Data
public class RxReviewItemVO {

    /** 主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 批次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long batchId;

    /** 批次号 */
    private String batchNo;

    /** 处方ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /** 处方号（快照） */
    private String prescriptionNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 开方科室（快照） */
    private String deptName;

    /** 开方医生ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /** 开方医生姓名（快照） */
    private String doctorName;

    /** 就诊日期 */
    private LocalDate visitDate;

    /** 诊断（快照） */
    private String diagnosis;

    /** 药品数量（快照） */
    private Integer drugCount;

    /** 处方金额（快照） */
    private BigDecimal totalAmount;

    /** 处方类型（1-西药 2-中成药 3-中药饮片） */
    private Integer prescriptionType;

    /** 处方来源（1-门诊 2-急诊 3-住院） */
    private Integer prescriptionSource;

    /** 点评状态（0-待点评 1-已点评） */
    private Integer reviewStatus;

    /** 点评结论（1-合理 2-不规范处方 3-用药不适宜处方 4-超常处方） */
    private Integer reviewResult;

    /** 问题码（11-15不规范 21-27不适宜 31-34超常） */
    private String problemTypes;

    /** 点评意见 */
    private String reviewOpinion;

    /** 点评人姓名 */
    private String reviewerName;

    /** 点评时间 */
    private LocalDateTime reviewTime;

    /** 公示状态（0-未公示 1-已公示） */
    private Integer publicityStatus;

    /** 公示操作人 */
    private String publicityBy;

    /** 公示时间 */
    private LocalDateTime publicityTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 处方药品明细文本（「药名规格，数量单位，用法（频次）」一行一药） */
    private List<String> drugDetails;
}
