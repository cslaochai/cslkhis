package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 审核检查报告入参
 */
@Data
public class InspectionAuditDTO {
    /**
     * 检查/检验记录ID
     */
    private Long recordId;

    /**
     * 审核人（可选）
     */
    private String auditBy;
}
