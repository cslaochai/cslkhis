package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医技执行记录查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExecutionQueryPageDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 申请类型：1-检查 2-检验
     */
    private Integer applyType;

    /**
     * 执行状态：1-待执行 2-执行中 3-已完成 4-已审核
     */
    private Integer executionStatus;
}
