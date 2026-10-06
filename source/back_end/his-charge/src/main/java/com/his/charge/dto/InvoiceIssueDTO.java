package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 出票入参（L4）。
 *
 * <p>只给账单ID：票面金额、患者、金额构成一律服务端从账单与支付流水现取，
 * 信前端传来的金额开票等于允许开出一张对不上钱的票。
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
