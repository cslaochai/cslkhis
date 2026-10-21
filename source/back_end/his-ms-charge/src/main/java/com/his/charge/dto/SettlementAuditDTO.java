package com.his.charge.dto;

import lombok.Data;

/**
 * 审核医保结算清单入参
 */
@Data
public class SettlementAuditDTO {

    /**
     * 结算清单ID
     */
    private Long id;

    /**
     * 是否审核通过
     */
    private Boolean approved;

    /**
     * 审核备注
     */
    private String remark;

}
