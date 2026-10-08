package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品供应商退货单（药离开医院，sql/154 三级链的③级）
 *
 * <p>它与「药房退回药库」的根本区别是：<b>货出了医院大门</b>。所以退货必须知道退给谁
 * （supplier_id 必填且与批次上的供应商一致），金额按批次成本价算，是向供应商主张退款的依据。
 * <p>它不动患者资金，也不动收费四层：退回来的钱走 L3 支付流水的负向记录，与本单无关。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_drug_supplier_return")
public class BizDrugSupplierReturn extends BaseEntity {
    /** 退货单号 */
    private String returnNo;

    /** 供应商ID（供应商主档主键） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;

    /** 供应商名称 */
    private String supplierName;

    /** 退货原因（近效期 / 质量问题 / 冷链断链 / 采购让价退货…，必填） */
    private String returnReason;

    /** 原入库单号/采购单号（人工填的溯源线索，可空） */
    private String srcRefNo;

    /** 状态（1-待退货 2-已退货 3-已作废，SupplierReturnStatusEnum） */
    private Integer status;

    /** 批次数 */
    private Integer totalItems;

    /** 退货合计数量 */
    private BigDecimal totalQuantity;

    /** 退货合计金额（Σ数量×批次成本价） */
    private BigDecimal totalAmount;

    /** 退货经办人 */
    private String returnBy;

    /** 退货时间 */
    private LocalDateTime returnTime;

    /** 作废操作人 */
    private String cancelBy;

    /** 作废时间 */
    private LocalDateTime cancelTime;

    /** 作废原因 */
    private String cancelReason;
}
