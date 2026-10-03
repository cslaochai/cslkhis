package com.his.pharmacy.dto;

import lombok.Data;

/**
 * 取药动作入参
 */
@Data
public class DrugPickUpDTO {
    /**
     * 发药单ID
     */
    private Long dispensingId;
    /**
     * 取药人
     */
    private String pickUpBy;
}
