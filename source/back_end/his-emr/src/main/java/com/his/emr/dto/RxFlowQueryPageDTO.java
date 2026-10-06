package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.EqualsAndHashCode;
import lombok.Data;

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
