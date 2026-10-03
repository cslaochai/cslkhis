package com.his.pharmacy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 入库单明细入参
 *
 * 药品编码/名称/规格/单位由服务端在生成时从药品字典**快照**写入，
 * 前端传了也不采信（否则可以伪造一张写着别的药名的入库凭证）。
 */
@Data
public class DrugInboundItemDTO {

    /** 药品ID */
    @NotNull(message = "入库明细的药品不能为空")
    private Long drugId;

    /** 批号 */
    @NotBlank(message = "批号不能为空")
    @Size(max = 50, message = "批号最长 50 位")
    private String batchNo;

    /** 生产日期 */
    private LocalDate productionDate;

    /** 有效期 */
    @NotNull(message = "有效期不能为空")
    private LocalDate expiryDate;

    @NotNull(message = "入库数量不能为空")
    @DecimalMin(value = "0.01", message = "入库数量必须大于 0")
    private BigDecimal quantity;

    /** 成本价 */
    @NotNull(message = "成本价不能为空")
    @DecimalMin(value = "0.00", message = "成本价不能为负数")
    private BigDecimal costPrice;

    /** 备注 */
    @Size(max = 500, message = "明细备注最长 500 位")
    private String remark;
}
