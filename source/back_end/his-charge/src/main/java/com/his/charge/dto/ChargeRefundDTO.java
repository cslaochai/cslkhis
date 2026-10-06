package com.his.charge.dto;

import lombok.Data;

/**
 * 退费入参
 */
@Data
public class ChargeRefundDTO {

    /**
     * 收费单ID
     */
    private Long chargeId;

    /**
     * 退费原因
     */
    private String reason;

    /**
     * 退费操作人
     */
    private String refundBy;

}
