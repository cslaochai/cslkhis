package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出院带药 VO。
 */
@Data
public class DischargeDrugVO {

    /**
     * 带药单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 带药单号
     */
    private String orderNo;

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
     * 患者号
     */
    private String patientNo;
    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;
    /**
     * 药品名称
     */
    private String drugName;
    /**
     * 规格
     */
    private String spec;
    /**
     * 每次剂量/用法用量描述
     */
    private String dosage;
    /**
     * 单位
     */
    private String unit;
    /**
     * 带药数量
     */
    private BigDecimal quantity;
    /**
     * 用药医嘱
     */
    private String usageText;
    /**
     * 用药天数
     */
    private Integer days;
    /**
     * 备注
     */
    private String remark;

    /**
     * 发药状态（1-待发药 2-已发药）
     */
    private Integer dispenseStatus;

    /**
     * 发药状态文案（his_discharge_drug_status）
     */
    private String dispenseStatusText;

    /**
     * 发药人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispenseBy;
    /**
     * 发药人姓名
     */
    private String dispenseName;
    /**
     * 发药时间
     */
    private LocalDateTime dispenseTime;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
