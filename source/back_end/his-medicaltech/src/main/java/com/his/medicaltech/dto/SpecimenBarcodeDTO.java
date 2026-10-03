package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 分配标本条码入参
 */
@Data
public class SpecimenBarcodeDTO {
    /**
     * 检验记录ID
     */
    private Long recordId;

    /**
     * 标本条码号
     */
    private String specimenNo;
}
