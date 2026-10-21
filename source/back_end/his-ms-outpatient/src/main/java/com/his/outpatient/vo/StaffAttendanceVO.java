package com.his.outpatient.vo;

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
 */
@Data
@Schema(description = "出勤登记结果")
public class StaffAttendanceVO implements Serializable {

    /** 出勤记录ID */
    @Schema(description = "出勤记录ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 对应的排班事实ID（无计划的加班/支援为 null） */
    @Schema(description = "对应的排班事实ID（无计划的加班/支援为 null）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    /** 员工ID */
    @Schema(description = "员工ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /** 姓名（登记时快照） */
    @Schema(description = "姓名（登记时快照）")
    private String employeeName;

    /** 工号（登记时快照） */
    @Schema(description = "工号（登记时快照）")
    private String empCode;

    /** 排班日期 */
    @Schema(description = "出勤日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /** 实际出勤单元类型（1-科室 2-病区 3-全院） */
    @Schema(description = "实际出勤单元类型（1-科室 2-病区 3-全院）")
    private Integer orgType;

    /** 实际出勤单元ID（全院级为 0） */
    @Schema(description = "实际出勤单元ID（全院级为 0）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /** 出勤单元名称 */
    @Schema(description = "单元名称")
    private String orgName;

    /** 班次ID（0-无班次） */
    @Schema(description = "班次ID（0-无班次）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /** 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他 7-护工） */
    @Schema(description = "岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政）")
    private Integer staffType;

    /** 签到时间（null=没签到） */
    @Schema(description = "签到时间（null=没签到）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkIn;

    /** 签退时间（null=还没签退或缺勤） */
    @Schema(description = "签退时间（null=还没签退或缺勤）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOut;

    /** 实际工时（分钟） */
    @Schema(description = "实际工时（分钟）")
    private Integer actualMinutes;

    /** 计划工时（分钟） */
    @Schema(description = "计划工时（分钟）")
    private Integer plannedMinutes;

    /** 超时工时（分钟） */
    @Schema(description = "超时工时（分钟）")
    private Integer overtimeMinutes;

    /** 出勤状态（1-正常 2-迟到 3-早退 4-缺勤 5-替班 6-加班 7-支援） */
    @Schema(description = "出勤状态（1-正常 2-迟到 3-早退 4-缺勤 5-替班 6-加班 7-支援）")
    private Integer attendanceStatus;

    /** 出勤状态文案（由枚举 label 渲染） */
    @Schema(description = "出勤状态文案")
    private String attendanceStatusText;

    /** 替了谁的班（员工ID） */
    @Schema(description = "替了谁的班（员工ID）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long substituteFor;

    /** 科室确认（0-待确认 1-已确认 2-有异议） */
    @Schema(description = "科室确认（0-待确认 1-已确认 2-有异议）")
    private Integer confirmStatus;

    /** 确认经办人 */
    @Schema(description = "确认人")
    private String confirmBy;

    /** 确认时间 */
    @Schema(description = "确认时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;

    /** 数据来源（1-人工登记 2-考勤机导入 3-系统判定） */
    @Schema(description = "数据来源（1-人工登记 2-考勤机导入 3-系统判定）")
    private Integer dataSource;

    /** 状态（0-停用 1-生效） */
    @Schema(description = "状态（0-停用 1-生效）")
    private Integer status;

    /** 本次操作的结论文案 */
    @Schema(description = "本次操作的结论文案")
    private String message;
}
