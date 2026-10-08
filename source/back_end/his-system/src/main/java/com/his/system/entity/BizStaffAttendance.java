package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实际出勤（某个人 · 某天 · 某个单元 · 某个班次，实际干了多久）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_staff_attendance")
public class BizStaffAttendance extends BaseEntity {

    /**
     * 关联的排班事实ID（biz_staff_schedule.id）；空=无计划的出勤（加班/支援/替班）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    /**
     * 员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 姓名
     */
    private String employeeName;

    /**
     * 工号
     */
    private String empCode;

    /**
     * 出勤日期（归属哪一天；夜班签退跨到次日也算这天）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /**
     * 实际出勤单元类型（1-科室 2-病区 3-全院）
     */
    private Integer orgType;

    /**
     * 实际出勤单元ID（全院级为 0）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /**
     * 单元名称
     */
    private String orgName;

    /**
     * 班次ID（0-无班次，如自由工时的加班）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政）
     */
    private Integer staffType;

    /**
     * 签到时间（NULL=没签到：缺勤确认或手工登记的工时）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkIn;

    /**
     * 签退时间（NULL=还没签退或缺勤）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOut;

    /**
     * 实际工时（分钟）：打卡则算，无打卡由科室确认后手工填
     */
    private Integer actualMinutes;

    /**
     * 计划工时（分钟）：biz_staff_schedule.work_minutes 的快照
     */
    private Integer plannedMinutes;

    /**
     * 超时工时（分钟）：GREATEST(0, 实际 - 计划)
     */
    private Integer overtimeMinutes;

    /**
     * 出勤状态（1-正常 2-迟到 3-早退 4-缺勤 5-替班 6-加班 7-支援）
     */
    private Integer attendanceStatus;

    /**
     * 替了谁的班（employee_id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long substituteFor;

    /**
     * 科室确认（0-待确认 1-已确认 2-有异议）
     */
    private Integer confirmStatus;

    /**
     * 确认人
     */
    private String confirmBy;

    /**
     * 确认时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;

    /**
     * 数据来源（1-人工登记 2-考勤机导入 3-系统判定）
     */
    private Integer dataSource;

    /**
     * 状态（0-停用 1-生效）
     */
    private Integer status;
}
