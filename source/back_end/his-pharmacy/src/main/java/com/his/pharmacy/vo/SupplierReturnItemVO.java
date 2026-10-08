package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 药品供应商退货明细出参
 */
@Data
public class SupplierReturnItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 退货单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long returnId;

    /** 库存批次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;

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

    /** 有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    /** 退货库位（1-药库 2-药房） */
    private Integer stockRoom;

    private String stockRoomText;

    /** 批次所属供应商ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;

    /** 批次成本价（快照，退货金额口径） */
    private BigDecimal costPrice;

    private BigDecimal quantity;

    /** 退货金额（数量×成本价） */
    private BigDecimal amount;

    /** 批次当前余额（退货后剩余，看清是不是把整批退完） */
    private BigDecimal currentQuantity;

    /** 备注 */
    private String remark;
}
