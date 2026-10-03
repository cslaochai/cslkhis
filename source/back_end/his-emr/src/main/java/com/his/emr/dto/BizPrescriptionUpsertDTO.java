package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 处方新增/修改入参
 */
@Data
public class BizPrescriptionUpsertDTO {
    /**
     * 处方ID，新增时为空，修改时必填
     */
    private Long prescriptionId;

    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 患者ID
     */
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
     * 性别
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 挂号ID
     */
    private Long registId;

    /**
     * 病历ID
     */
    private Long recordId;

    /**
     * 病历号
     */
    private String recordNo;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 处方类型（1-西药处方 2-中成药处方 3-中药饮片处方）
     */
    private Integer prescriptionType;

    /**
     * 处方来源（1-门诊处方 2-急诊处方 3-住院处方）
     */
    private Integer prescriptionSource;

    /**
     * 总金额，单位：元
     */
    private BigDecimal totalAmount;

    /**
     * 药品数量
     */
    private Integer drugCount;

    /**
     * 用法说明
     */
    private String usageInstruction;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 处方状态（1-草稿 2-已提交 3-已审核 4-已发药 5-已取消 6-已退药）
     */
    private Integer prescriptionStatus;

    /**
     * 缴费状态（0-未缴费 1-已缴费 2-已退费）
     */
    private Integer paymentStatus;

    /**
     * 缴费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /**
     * 实付金额，单位：元
     */
    private BigDecimal payAmount;

    /**
     * 支付方式（1-现金 2-微信 3-支付宝 4-医保卡 5-余额）
     */
    private Integer payMethod;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 是否加急（0-否 1-是）
     */
    private Integer isUrgent;

    /**
     * 处方明细列表
     */
    private List<BizPrescriptionDetailUpsertDTO> details;
}
