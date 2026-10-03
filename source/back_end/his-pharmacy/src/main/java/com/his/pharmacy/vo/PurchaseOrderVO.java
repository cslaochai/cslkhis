package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单出参
 */
@Data
public class PurchaseOrderVO {

    /** 采购订单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /** 采购订单号 */
    private String orderNo;

    /** 供应商ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;

    /** 供应商名称（联表带出） */
    private String supplierName;

    /** 供应商编码（联表带出） */
    private String supplierCode;

    /** 下单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    /** 订单总金额 = Σ明细金额 */
    private BigDecimal totalAmount;

    /** 审批状态（0-待审批 1-已通过 2-已驳回） */
    private Integer approvalStatus;

    /** 审批人ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long approverId;

    /** 最近一张入库单号（派生自药品入库单；未生成入库单时为 null） */
    private String inboundNo;

    /** 是否已入库（存在状态=3 的入库单）—— 派生事实，订单表不存 */
    private Boolean inboundDone;

    /** 明细条数（列表带出） */
    private Integer itemCount;

    /** 明细数量合计（列表带出） */
    private BigDecimal totalQuantity;

    /** 备注 */
    private String remark;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 采购明细（仅详情接口填充） */
    private List<PurchaseOrderDetailVO> items;
}
