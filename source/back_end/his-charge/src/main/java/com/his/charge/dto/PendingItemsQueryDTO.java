package com.his.charge.dto;

import lombok.Data;

/**
 * 待缴费项目查询入参
 */
@Data
public class PendingItemsQueryDTO {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 挂号ID快照
     */
    private Long registId;

}
