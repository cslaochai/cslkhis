package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 患者端「我的处方/取药」列表项。
 */
@Data
public class MyPrescriptionVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 处方类型（1-西药处方 2-中成药处方 3-中药饮片处方）
     */
    private Integer prescriptionType;

    private String prescriptionTypeText;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 用法说明
     */
    private String usageInstruction;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 药品数量
     */
    private Integer drugCount;

    /**
     * 处方状态（1-草稿 2-已提交 3-已审核 4-已发药 5-已取消 6-已退药）
     */
    private Integer prescriptionStatus;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 缴费状态（0-未缴费 1-已缴费 2-已退费）
     */
    private Integer paymentStatus;

    private String paymentStatusText;

    /**
     * 发药时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dispenseTime;

    private List<MyPrescriptionDetailVO> details;
}
