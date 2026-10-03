package com.his.pharmacy.dto;

import lombok.Data;

/**
 * 发药动作入参
 */
@Data
public class DrugDispenseDTO {
    /**
     * 发药单ID
     */
    private Long dispensingId;
    /**
     * 发药人
     */
    private String dispenseBy;
}
