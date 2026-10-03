package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验记录查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LaboratoryRecordQueryDTO extends PageParam {
    /**
     * 患者ID（可选）
     */
    private Long patientId;

    /**
     * 执行检验科室ID（可选）
     */
    private Long laboratoryDeptId;
}
