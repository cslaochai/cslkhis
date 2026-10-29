package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 审核检验报告入参
 */
@Data
public class LaboratoryAuditDTO {
    /**
     * 检查/检验记录ID
     */
    private Long recordId;

    /**
     * 审核人（可选）
     */
    private String auditBy;

    /**
     * 签名 PIN（软签二次确认）：签名人本人输入，明文不入库
     */
    private String pin;
}
