package com.his.charge.dto;

import lombok.Data;

/**
 * 审核退费申请入参
 */
@Data
public class RefundApplyAuditDTO {

    /**
     * 退费申请ID
     */
    private Long id;

    /**
     * 是否审核通过
     */
    private Boolean approved;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核人姓名
     */
    private String auditorName;

    /**
     * 审核备注
     */
    private String remark;

}
