package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台数字卡「病区今日概况」（{@code WorkbenchMetricMapper#nurseStats}）。
 *
 * <p>字段名是前端契约（{@code METRIC_SPECS.wardNursingToday}），{@code todoExecCount}
 * 标了 {@code danger: true}（待执行医嘱积压）。
 *
 * <p>病区由当前 {@code deptId} 推导（病区的科室ID = 护士主科室）：护士未切科室时 deptId 为空，
 * SQL 的 {@code IN (SELECT ... WHERE dept_id = NULL)} 匹配 0 行 → 全为 0，而不是抛异常。
 *
 * <p>床位/在院只认床位的床位状态与入院记录的入院状态：病区表上的"已占床位数 / 床位总数"
 * 是演示假数据，禁止作依据。
 */
@Data
public class WorkbenchNurseStatsRowVO implements Serializable {

    /**
     * 病区在院人数
     */
    private Long wardInpatientCount;

    /**
     * 病区床位总数
     */
    private Long bedTotal;

    /**
     * 病区占用床位数
     */
    private Long bedOccupied;

    /**
     * 今日入院人数
     */
    private Long todayAdmitCount;

    /**
     * 今日出院人数
     */
    private Long todayDischargeCount;

    /**
     * 待执行医嘱数
     */
    private Long todoExecCount;
}