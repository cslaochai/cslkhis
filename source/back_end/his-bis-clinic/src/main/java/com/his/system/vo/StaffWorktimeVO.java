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

    @Schema(description = "排班事实ID（无计划的出勤为 null）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;

    @Schema(description = "出勤记录ID（尚未登记为 null）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long attendId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    private String employeeName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    private Integer orgType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    private String orgName;

    private Integer staffType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    private String shiftName;

    private Integer dutyStatus;

    @Schema(description = "计划工时（分钟）")
    private Integer plannedMinutes;

    @Schema(description = "实际工时（分钟）")
    private Integer actualMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkIn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOut;

    private Integer attendanceStatus;

    private Integer confirmStatus;

    @Schema(description = "差异类型（1正常 2迟到 3早退 4工时不足 5超时加班 6缺勤 7未回填 8无计划出勤）")
    private Integer diffType;

    @Schema(description = "差异原因（写给人看的一句话）")
    private String diffReason;

    @Schema(description = "工时差（分钟，正数=超时）")
    private Integer diffMinutes;
}
