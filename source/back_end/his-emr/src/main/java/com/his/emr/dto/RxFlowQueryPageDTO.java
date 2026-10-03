package com.his.emr.dto;

import lombok.Data;

/**
 * 处方流转单分页查询入参
 */
@Data
public class RxFlowQueryPageDTO {
    private Long patientId;
    private Long prescriptionId;
    private Integer flowStatus;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
