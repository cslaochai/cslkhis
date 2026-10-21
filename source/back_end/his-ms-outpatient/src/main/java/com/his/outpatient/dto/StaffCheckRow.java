package com.his.outpatient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 员工排班区间校验用行（含班次夜班标记，规则校验一次捞齐避免逐人查询）。
 */
@Data
public class StaffCheckRow {

    /** 排班记录ID */
    private Long id;
    /** 排班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;
    /** 排班单元类型 */
    private Integer orgType;
    /** 排班单元ID */
    private Long orgId;
    /** 排班单元名称 */
    private String orgName;
    /** 员工ID */
    private Long employeeId;
    /** 工号 */
    private String empCode;
    /** 姓名 */
    private String employeeName;
    /** 岗位类别 */
    private Integer staffType;
    /** 班次ID（0-无班次） */
    private Long shiftId;
    /** 班次名称 */
    private String shiftName;
    /** 开始时间 HH:mm */
    private String startTime;
    /** 结束时间 HH:mm */
    private String endTime;
    /** 工时（分钟） */
    private Integer workMinutes;
    /** 出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班） */
    private Integer dutyStatus;
    /** 发布态 */
    private Integer publishStatus;
    /** 是否夜班（0-否 1-是，取自班次字典） */
    private Integer isNight;
}
