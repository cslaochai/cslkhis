package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发票查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InvoiceQueryPageDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 结算账单ID
     */
    private Long billId;

    /**
     * 发票状态（1-已开具 2-已打印 3-已作废 4-已红冲换开）
     */
    private Integer invoiceStatus;

    /**
     * 关键字：发票号 / 账单号 / 患者姓名模糊匹配
     */
    private String keyword;
}
