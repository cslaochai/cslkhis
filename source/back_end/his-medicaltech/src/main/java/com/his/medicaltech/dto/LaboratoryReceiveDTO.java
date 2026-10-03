package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 接收标本入参
 */
@Data
public class LaboratoryReceiveDTO {
    /**
     * 检验记录ID
     */
    private Long recordId;

    /**
     * 接收人（可选）
     */
    private String receiveBy;
}
