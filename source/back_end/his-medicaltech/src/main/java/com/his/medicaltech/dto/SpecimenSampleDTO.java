package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 标本采集确认入参
 */
@Data
public class SpecimenSampleDTO {
    /**
     * 检验记录ID
     */
    private Long recordId;

    /**
     * 采集人（可选）
     */
    private String sampleBy;
}
