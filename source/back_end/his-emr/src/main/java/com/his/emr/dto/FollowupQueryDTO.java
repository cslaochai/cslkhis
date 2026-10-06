package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 随访任务查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FollowupQueryDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 随访类型（1-复诊提醒 2-慢病随访 3-用药指导 4-术后随访）
     */
    private Integer followupType;

    /**
     * 随访状态：1-待随访 2-随访中 3-已完成 4-已取消
     */
    private Integer followupStatus;

    /**
     * 患者姓名模糊（G20）
     */
    private String patientName;

    /**
     * 随访科室（越权科室由后端 DeptScopeProvider 直接拒绝，不做静默改写）
     */
    private Long deptId;

    /**
     * 仅看逾期未随访（待随访/随访中且计划时间已过）
     */
    private Boolean overdueOnly;
}
