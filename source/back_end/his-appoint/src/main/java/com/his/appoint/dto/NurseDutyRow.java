package com.his.appoint.dto;

import lombok.Data;

/**
 * 排班底座
 *
 */
@Data
public class NurseDutyRow {

    /**
     * 排班事实行
     */
    private Long id;

    /**
     * 护士员工 ID
     */
    private Long employeeId;

    /**
     * 护士姓名
     */
    private String nurseName;

    /**
     * 科室 ID（unit_type=2 门诊场景冗余写科室 ID；=1 病区场景写所属科室 ID）
     */
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 班次 ID
     */
    private Long shiftId;

    /**
     * 开始时间 HH:mm
     */
    private String startTime;

    /**
     * 结束时间 HH:mm
     */
    private String endTime;

    /**
     * 出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班）
     */
    private Integer scheduleStatus;
}
