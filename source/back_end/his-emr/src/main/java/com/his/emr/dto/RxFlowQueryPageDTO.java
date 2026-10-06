package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 处方流转单分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RxFlowQueryPageDTO extends PageParam {
    private Long patientId;
    private Long prescriptionId;
    private Integer flowStatus;

}
