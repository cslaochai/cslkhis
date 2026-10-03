package com.his.emr.dto;

import lombok.Data;

/**
 * 审核公卫上报入参
 */
@Data
public class PublicHealthAuditDTO {

    /**
     * 上报记录ID
     */
    private Long id;

    /**
     * 是否审核通过
     */
    private Boolean approved;

    /**
     * 审核人
     */
    private String auditBy;

    /**
     * 审核备注
     */
    private String remark;

}
