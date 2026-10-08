package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 代煎台账列表行（sql/139）
 */
@Data
public class TcmDecoctVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 代煎单号
     */
    private String decoctNo;

    /**
     * 处方ID
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
     * 剂数
     */
    private Integer doseCount;

    /**
     * 味数
     */
    private Integer herbCount;

    /**
     * 全方总克数
     */
    private BigDecimal totalGrams;

    /**
     * 每剂平均克数（总克数 ÷ 剂数，煎药室按这个量加水）
     */
    private BigDecimal gramsPerDose;

    /**
     * 煎法脚注汇总
     */
    private String methodSummary;

    /**
     * 状态（1-待煎 2-已煎 3-已取 9-已作废）
     */
    private Integer decoctStatus;

    /**
     * 状态名（服务端翻译，前端不再自造一套码）
     */
    private String decoctStatusLabel;

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
     * 作废原因
     */
    private String cancelReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
