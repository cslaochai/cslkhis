package com.his.pharmacy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购订单明细入参
 *
 * 明细必须带批号与有效期：入库时按「药品+批号」建/找批次，有效期缺失就无法建批次
 * （药品批次库存的有效期是必填列），采购链会在最后一步断掉。
 */
@Data
public class PurchaseOrderDetailUpsertDTO {

    /** 药品ID */
    @NotNull(message = "采购明细的药品不能为空")
    private Long drugId;

    /** 采购数量 */
    @NotNull(message = "采购数量不能为空")
    @DecimalMin(value = "0.01", message = "采购数量必须大于 0")
    private BigDecimal quantity;

    /** 采购单价 */
    @NotNull(message = "采购单价不能为空")
    @DecimalMin(value = "0.00", message = "采购单价不能为负数")
    private BigDecimal unitPrice;

    /** 批号 */
    @NotBlank(message = "批号不能为空")
    @Size(max = 50, message = "批号最长 50 位")
    private String batchNo;

    /** 生产日期 */
    private LocalDate productionDate;

    /** 有效期 */
    @NotNull(message = "有效期不能为空")
    private LocalDate expiryDate;

    /** 备注 */
    @Size(max = 500, message = "明细备注最长 500 位")
    private String remark;
}
