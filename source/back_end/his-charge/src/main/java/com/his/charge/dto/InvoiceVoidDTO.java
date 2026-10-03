package com.his.charge.dto;

import lombok.Data;

/**
 * 作废发票入参
 */
@Data
public class InvoiceVoidDTO {

    /**
     * 发票ID
     */
    private Long id;

    /**
     * 作废原因
     */
    private String reason;

}
