package com.his.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 入库单生成入参
 *
 * ⚠ 金额不从这里收：明细 amount 与单头 total_amount / total_quantity 一律服务端重算。
 */
@Data
public class DrugInboundCreateDTO {

    /** 入库类型（1-采购入库 2-退货入库 3-盘盈入库 4-其他入库） */
    @NotNull(message = "入库类型不能为空")
    @Min(value = 1, message = "入库类型取值不合法（1-采购入库 2-退货入库 3-盘盈入库 4-其他入库）")
    @Max(value = 4, message = "入库类型取值不合法（1-采购入库 2-退货入库 3-盘盈入库 4-其他入库）")
    private Integer inboundType;

    /** 来源采购订单ID（采购入库时必填） */
    private Long purchaseOrderId;

    /** 来源采购订单号 */
    private String purchaseOrderNo;

    /** 供应商名称（文本快照） */
    @Size(max = 200, message = "供应商名称最长 200 位")
    private String supplier;

    /** 备注 */
    @Size(max = 500, message = "备注最长 500 位")
    private String remark;

    /** 明细项集合 */
    @NotEmpty(message = "入库明细不能为空，至少要有一条")
    @Valid
    private List<DrugInboundItemDTO> items;
}
