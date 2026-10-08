package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 单元 × 日 的执行汇总（对应视图 v_staff_worktime_summary）。
 */
@Data
@Schema(description = "单元 × 日 执行汇总")
public class WorktimeSummaryVO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    private Integer orgType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    private String orgName;

    private Integer staffType;

    @Schema(description = "排了上班的人数")
    private Integer planHead;

    @Schema(description = "实际到岗人数")
    private Integer presentHead;

    @Schema(description = "已确认缺勤人数")
    private Integer absentHead;

    @Schema(description = "未回填人数（有排班无出勤记录，需催科室登记）")
    private Integer unrecordedHead;

    @Schema(description = "无排班却来上班的人数")
    private Integer extraHead;

    @Schema(description = "计划工时合计（分钟）")
    private Long plannedMinutes;

    @Schema(description = "实际工时合计（分钟）")
    private Long actualMinutes;

    @Schema(description = "工时差（分钟）")
    private Long diffMinutes;

    @Schema(description = "工时达成率（%）")
    private BigDecimal fulfillRate;
}
