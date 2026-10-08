package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 出票入参（L4）。
 */
@Data
public class InvoiceIssueDTO {

    /**
     * 结算账单ID
     */
    @NotNull(message = "缺少结算账单")
    private Long billId;

    /**
     * 发票类型（字典 his_invoice_type：1-普通发票 2-电子发票 3-数电发票），为空按 1
     */
    private Integer invoiceType;

    /**
     * 备注
     */
    private String remark;
}
