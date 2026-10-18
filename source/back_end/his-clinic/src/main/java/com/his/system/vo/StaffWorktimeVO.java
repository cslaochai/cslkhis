package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 计划 vs 实际 的行级对照结果（对应视图 v_staff_worktime）。
 */
@Data
@Schema(description = "计划 vs 实际对照行")
public class StaffWorktimeVO {

    /** 排班事实ID（无计划的出勤为 null） */
    @Schema(description = "排班事实ID（无计划的出勤为 null）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;

    /** 出勤记录ID（尚未登记为 null） */
    @Schema(description = "出勤记录ID（尚未登记为 null）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long attendId;

    /** 员工ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /** 员工姓名 */
    private String employeeName;

    /** 出勤日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    /** 排班单元类型（1-科室 2-病区 3-全院） */
    private Integer orgType;

    /** 排班单元ID（全院级为 0） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /** 排班单元名称 */
    private String orgName;

    /** 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他 7-护工） */
    private Integer staffType;

    /** 班次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /** 班次名称 */
    private String shiftName;

    /** 出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班） */
    private Integer dutyStatus;

    /** 计划工时（分钟） */
    @Schema(description = "计划工时（分钟）")
    private Integer plannedMinutes;

    /** 实际工时（分钟） */
    @Schema(description = "实际工时（分钟）")
    private Integer actualMinutes;

    /** 签到时间（null=没签到） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkIn;

    /** 签退时间（null=还没签退或缺勤） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOut;

    /** 出勤判定（1-正常 2-迟到 3-早退 4-缺勤 5-替班 6-加班 7-支援） */
    private Integer attendanceStatus;

    /** 科室确认（0-待确认 1-已确认 2-有异议） */
    private Integer confirmStatus;

    /** 差异类型（1-正常 2-迟到 3-早退 4-工时不足 5-超时加班 6-缺勤 7-未回填 8-无计划出勤） */
    @Schema(description = "差异类型（1正常 2迟到 3早退 4工时不足 5超时加班 6缺勤 7未回填 8无计划出勤）")
    private Integer diffType;

    /** 差异原因（写给人看的一句话） */
    @Schema(description = "差异原因（写给人看的一句话）")
    private String diffReason;

    /** 工时差（分钟，正数=超时） */
    @Schema(description = "工时差（分钟，正数=超时）")
    private Integer diffMinutes;
}
