package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 出勤登记入参（签到 / 签退 / 缺勤确认 / 手工修正四个入口共用）。
 */
@Data
@Schema(description = "出勤登记")
public class AttendanceDTO {

    @Schema(description = "员工ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long employeeId;

    @Schema(description = "出勤日期（不填=今天）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    @Schema(description = "实际出勤单元类型（1-科室 2-病区 3-全院）；不填=按排班计划")
    private Integer orgType;

    @Schema(description = "实际出勤单元ID；不填=按排班计划")
    private Long orgId;

    @Schema(description = "班次ID；不填=按排班计划")
    private Long shiftId;

    @Schema(description = "岗位类别（无计划出勤时必填）")
    private Integer staffType;

    @Schema(description = "签到时间（不填=此刻）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkIn;

    @Schema(description = "签退时间（不填=此刻）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOut;

    @Schema(description = "实际工时（分钟）—— 仅手工修正时由护士长填")
    private Integer actualMinutes;

    @Schema(description = "手工指定的出勤状态（1正常 2迟到 3早退 4缺勤 5替班 6加班 7支援）")
    private Integer attendanceStatus;

    @Schema(description = "替了谁的班（employee_id）")
    private Long substituteFor;

    @Schema(description = "备注（写清为什么会差这么多）")
    private String remark;
}
