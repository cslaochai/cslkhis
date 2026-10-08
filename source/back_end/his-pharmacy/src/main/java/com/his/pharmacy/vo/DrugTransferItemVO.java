package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 药品调拨明细出参
 */
@Data
public class DrugTransferItemVO {

    /** 主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 调拨单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long transferId;

    /** 发出方库存批次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;

    /** 接收方批次ID（未接收为 null） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inStockId;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 药品编码 */
    private String drugCode;

    /** 药品名称 */
    private String drugName;

    /** 规格 */
    private String specification;

    /** 单位 */
    private String unit;

    /** 批号 */
    private String batchNo;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate productionDate;

    /** 有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 调拨数量 */
    private BigDecimal applyQuantity;

    /** 建单时该批次已锁定量（快照，被闸门挡下时说明原因用） */
    private BigDecimal lockedQuantity;

    /** 调拨金额（数量×成本价） */
    private BigDecimal amount;

    /** 发出标记（0-未发出 1-已发出） */
    private Integer outFlag;

    /** 接收标记（0-未接收 1-已接收） */
    private Integer inFlag;

    /** 发出方批次当前余额（发出后应为 0 或减少，接收前一眼看出是否被别的单据动过） */
    private BigDecimal fromQuantity;

    /** 接收方批次当前余额 */
    private BigDecimal toQuantity;

    /** 备注 */
    private String remark;
}
