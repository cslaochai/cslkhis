package com.his.operation.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 住院手术申请分页查询入参（命名遵循 AGENTS.md：分页查询用 `xxxQueryPageDTO`）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperationApplyQueryPageDTO extends PageParam implements Serializable {

    /**
     * 入院ID（为空 = 不按住院过滤，手术室工作台就是全院）
     */
    private Long admissionId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 申请科室ID
     */
    private Long applyDeptId;

    /**
     * 主刀医师ID（员工ID）
     */
    private Long surgeonId;

    /**
     * 状态（0-待排期 1-已排期 2-术前核对完成 3-已完成 4-已取消）
     */
    private Integer operationStatus;

    /**
     * 手术间
     */
    private String operationRoom;

    /**
     * 计划开始时间下界（含）。
     */
    private String plannedDateFrom;

    /**
     * 计划开始时间上界（含当天；服务端会放宽到"次日 00:00:00 且不含"）
     */
    private String plannedDateTo;

    /**
     * 关键字（手术单号 / 入院号 / 患者姓名 / 患者号 / 术式 / 主刀）
     */
    private String keyword;

    /**
     * 只看未完成（0-待排期 1-已排期 2-术前核对完成）：1-是
     */
    private Integer unfinishedOnly;
}
