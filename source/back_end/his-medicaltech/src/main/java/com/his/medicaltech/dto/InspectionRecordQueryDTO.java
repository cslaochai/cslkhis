package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检查记录查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionRecordQueryDTO extends PageParam {
    /**
     * 患者ID（可选）
     */
    private Long patientId;

    /**
     * 执行检查科室ID（可选）
     */
    private Long inspectionDeptId;
}
