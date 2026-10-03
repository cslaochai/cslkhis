package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购订单明细出参（带药品字典信息）
 */
@Data
public class PurchaseOrderDetailVO {

    /** 明细ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 采购订单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 药品编码（药品字典联表带出） */
    private String drugCode;

    /** 药品名称（药品字典联表带出） */
    private String drugName;

    /** 规格（药品字典联表带出） */
    private String specification;

    /** 单位（药品字典联表带出） */
    private String unit;

    /** 采购数量 */
    private BigDecimal quantity;

    /** 采购单价 */
    private BigDecimal unitPrice;

    /** 金额 = 数量 × 单价 */
    private BigDecimal amount;

    /** 批号 */
    private String batchNo;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate productionDate;

    /** 有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
