package com.his.pharmacy.dto;

import lombok.Data;

/**
 * 退药入参
 */
@Data
public class DrugReturnDTO {

    /**
     * 发药记录ID
     */
    private Long id;

    /**
     * 退药原因
     */
    private String reason;

}
