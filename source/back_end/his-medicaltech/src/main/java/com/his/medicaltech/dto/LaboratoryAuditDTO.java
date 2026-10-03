package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 审核检验报告入参
 */
@Data
public class LaboratoryAuditDTO {
    /** 检查/检验记录ID */
    private Long recordId;

    /**
     * 审核人（可选）
     */
    private String auditBy;
}
