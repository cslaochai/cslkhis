package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 计划 vs 实际 的行级对照结果（对应视图 {@code v_staff_worktime}）。
 *
 * <p>一行就是「某个人某天的某个班」。<b>差异类型 {@code diffType} 是本视图的核心产出</b>：
 * <pre>
 *   1-正常        有排已到，工时差在 ±15 分钟内
 *   2-迟到        签到晚于「班次开始 + 宽限」
 *   3-早退        签退早于班次结束
 *   4-工时不足    到了但工时明显少于计划（迟到早退解释不了的，如中途离岗）
 *   5-超时/加班   实际工时明显多于计划（同一条计划上的延长）
 *   6-缺勤        <b>科室已确认的缺勤</b> —— 系统永远不会自己判
 *   7-未回填      排了班、日子也过了、一条出勤记录都没有 —— <b>不是缺勤，是没数</b>
 *   8-无计划出勤  没排班却来上班了（加班/支援/替班）
 * </pre>
 *
 * <p><b>为什么 6 和 7 必须分开</b>：这两件事的管理动作完全不同 ——
 * 缺勤要扣绩效，未回填要催科室登记。混在一起，护士长打开名单会看到一堆"缺勤"，
 * 逐个查完发现一半只是没打卡，这个表从此没人再看。
 */
@Data
@Schema(description = "计划 vs 实际对照行")
public class StaffWorktimeVO {

    @Schema(description = "排班事实ID（无计划的出勤为 null）")
    private String planId;

    @Schema(description = "出勤记录ID（尚未登记为 null）")
    private String attendId;

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
