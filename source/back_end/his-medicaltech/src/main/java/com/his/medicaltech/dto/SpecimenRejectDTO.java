package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 标本退回入参
 */
@Data
public class SpecimenRejectDTO {
    /**
     * 检验记录ID
     */
    private Long recordId;

    /**
     * 退回原因（可选）
     */
    private String reason;
}
