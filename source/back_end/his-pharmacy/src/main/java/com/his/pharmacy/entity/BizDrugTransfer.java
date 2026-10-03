package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品调拨单（药库 ↔ 药房，sql/154）
 *
 * <p>它回答的问题是「这批货现在在哪个库位」，不回答「谁该付钱」——调拨不动资金，
 * 所以在收费四层里没有它的位置，它是库存层（L1 之下）的单据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_drug_transfer")
public class BizDrugTransfer extends BaseEntity {
    /** 调拨单号 */
    private String transferNo;

    /** 方向（1-药库下拨药房 2-药房退回药库，DrugTransferTypeEnum） */
    private Integer transferType;

    /** 发出库位（按方向派生，冗余出来给列表直接看） */
    private Integer fromRoom;

    /** 接收库位 */
    private Integer toRoom;

    /** 事由（必填） */
    private String reason;

    /** 状态（1-待发出 2-待接收 3-已完成 4-已作废，DrugTransferStatusEnum） */
    private Integer status;

    /** 批次数 */
    private Integer totalItems;

    /** 申请合计数量 */
    private BigDecimal totalQuantity;

    /** 已发出合计数量 */
    private BigDecimal outQuantity;

    /** 已接收合计数量（等于已发出即账平） */
    private BigDecimal inQuantity;

    /** 合计金额（按批次成本价） */
    private BigDecimal totalAmount;

    /** 发出人 */
    private String outBy;

    /** 发出时间 */
    private LocalDateTime outTime;

    /** 接收人 */
    private String inBy;

    /** 接收时间 */
    private LocalDateTime inTime;

    /** 作废操作人 */
    private String cancelBy;

    /** 作废时间 */
    private LocalDateTime cancelTime;

    /** 作废原因 */
    private String cancelReason;
}
