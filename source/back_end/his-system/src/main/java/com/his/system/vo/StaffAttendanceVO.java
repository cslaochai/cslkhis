package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 一次出勤登记的结果（签到/签退/缺勤确认/工时修正四个写入口的出参）。
 *
 * <p>{@code message} 是本次操作的结论文案（含迟到/替班/加班/支援这类判定），
 * 由服务侧按出勤状态定 —— 前端只弹这一句就能给用户完整回执，不需要自己拼状态文案。
 */
@Data
@Schema(description = "出勤登记结果")
public class StaffAttendanceVO implements Serializable {

    @Schema(description = "出勤记录ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "对应的排班事实ID（无计划的加班/支援为 null）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    @Schema(description = "员工ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    @Schema(description = "姓名（登记时快照）")
    private String employeeName;

    @Schema(description = "工号（登记时快照）")
    private String empCode;

    @Schema(description = "出勤日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    @Schema(description = "实际出勤单元类型（1-科室 2-病区 3-全院）")
    private Integer orgType;

    @Schema(description = "实际出勤单元ID（全院级为 0）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    @Schema(description = "单元名称")
    private String orgName;

    @Schema(description = "班次ID（0-无班次）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    @Schema(description = "岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政）")
    private Integer staffType;

    @Schema(description = "签到时间（null=没签到）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkIn;

    @Schema(description = "签退时间（null=还没签退或缺勤）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOut;

    @Schema(description = "实际工时（分钟）")
    private Integer actualMinutes;

    @Schema(description = "计划工时（分钟）")
    private Integer plannedMinutes;

    @Schema(description = "超时工时（分钟）")
    private Integer overtimeMinutes;

    @Schema(description = "出勤状态（1-正常 2-迟到 3-早退 4-缺勤 5-替班 6-加班 7-支援）")
    private Integer attendanceStatus;

    @Schema(description = "出勤状态文案")
    private String attendanceStatusText;

    @Schema(description = "替了谁的班（员工ID）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long substituteFor;

    @Schema(description = "科室确认（0-待确认 1-已确认 2-有异议）")
    private Integer confirmStatus;

    @Schema(description = "确认人")
    private String confirmBy;

    @Schema(description = "确认时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;

    @Schema(description = "数据来源（1-人工登记 2-考勤机导入 3-系统判定）")
    private Integer dataSource;

    @Schema(description = "状态（0-停用 1-生效）")
    private Integer status;

    @Schema(description = "本次操作的结论文案")
    private String message;
}
