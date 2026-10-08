package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 高值耗材溯源台账列表VO
 */
@Data
public class BizConsumableTraceVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 院内追溯码
     */
    private String traceNo;
    /**
     * UDI 原文
     */
    private String udiCode;
    /**
     * 解析-产品标识
     */
    private String udiDi;
    /**
     * 解析-序列号
     */
    private String udiSerial;
    /**
     * 解析-批号
     */
    private String udiBatch;
    /**
     * 解析-有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate udiExpiryDate;
    /**
     * 耗材ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consumableId;
    /**
     * 耗材编码
     */
    private String consumableCode;
    /**
     * 耗材名称
     */
    private String consumableName;
    /**
     * 规格
     */
    private String specification;
    /**
     * 单位
     */
    private String unit;
    /**
     * 注册证号
     */
    private String regCertNo;
    /**
     * 计费单价快照
     */
    private BigDecimal retailPrice;
    /**
     * 出库批次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;
    /**
     * 批号
     */
    private String batchNo;
    /**
     * 供应商
     */
    private String supplier;
    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /**
     * 患者编号
     */
    private String patientNo;
    /**
     * 患者姓名
     */
    private String patientName;
    /**
     * 就诊类型（1-门诊 2-住院）
     */
    private Integer visitType;
    /**
     * 门诊挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;
    /**
     * 住院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
    /**
     * 使用科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 使用科室名称
     */
    private String deptName;
    /**
     * 使用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime usageTime;
    /**
     * 登记人
     */
    private String operatorName;
    /**
     * 计费状态（0-未计费 1-已计费 2-计费失败）
     */
    private Integer chargeStatus;
    /**
     * 记账单号
     */
    private String feeNo;
    /**
     * 未计费/计费失败原因
     */
    private String chargeFailReason;
    /**
     * 记录状态（1-使用中 2-已作废）
     */
    private Integer status;
    /**
     * 作废时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime voidTime;
    /**
     * 作废原因
     */
    private String voidReason;
    /**
     * 备注
     */
    private String remark;
}
