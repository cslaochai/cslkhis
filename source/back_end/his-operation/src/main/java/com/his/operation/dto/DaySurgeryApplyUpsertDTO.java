package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术预约登记入参（新增 / 修改，仅「待评估」可改）。
 */
@Data
public class DaySurgeryApplyUpsertDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 准入术式ID
     */
    @NotNull(message = "准入术式不能为空")
    private Long itemId;

    /**
     * 患者ID
     */
    @NotNull(message = "患者不能为空")
    private Long patientId;

    /**
     * 手术科室ID
     */
    private Long deptId;

    /**
     * 手术医生ID（员工ID）
     */
    private Long doctorId;

    /**
     * 计划手术日期 yyyy-MM-dd
     */
    @NotNull(message = "计划手术日期不能为空")
    private String planSurgeryDate;

    /**
     * 备注
     */
    private String remark;
}
