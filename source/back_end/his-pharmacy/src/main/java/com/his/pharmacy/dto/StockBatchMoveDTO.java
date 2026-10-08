package com.his.pharmacy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 批次库存移动指令（内部参数对象，不给前端）
 */
@Data
public class StockBatchMoveDTO {

    /** 源批次ID（扣减必填；接收时用于兜底快照） */
    private Long stockId;

    /** 药品ID（接收落位必填：同批号在目标库位不存在时按它建新批） */
    private Long drugId;

    /** 批号（接收落位必填） */
    private String batchNo;

    /** 生产日期（建新批快照） */
    private LocalDate productionDate;

    /** 有效期（建新批快照） */
    private LocalDate expiryDate;

    /** 成本价（建新批快照；调拨不改成本，退货按它算退货金额） */
    private BigDecimal costPrice;

    /** 供应商名称（建新批快照） */
    private String supplier;

    /** 供应商ID（建新批快照） */
    private Long supplierId;

    /** 目标库位（接收必填；扣减时用于校验批次确实在这个库位） */
    private Integer stockRoom;

    /** 数量（恒为正数，方向由 changeType 决定） */
    private BigDecimal quantity;

    /** 流水变动类型（DrugStockChangeTypeEnum 的 code：7-调拨出库 8-调拨入库 9-退货出库） */
    private Integer changeType;

    /** 来源单据类型（drugTransfer / supplierReturn） */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单据号 */
    private String sourceNo;

    /** 操作人 */
    private String operatorName;

    /** 流水备注（事由/原因，落库前截到列宽） */
    private String reason;
}
