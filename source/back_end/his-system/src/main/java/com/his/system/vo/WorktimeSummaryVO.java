package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 单元 × 日 的执行汇总（对应视图 {@code v_staff_worktime_summary}）。
 *
 * <p>这几个"人数"口径是<b>故意分成四个字段</b>的，合成一个大字段一定会被误读：
 * <pre>
 *   planHead        排了多少人上班
 *   presentHead     实际到岗（不含缺勤确认与未回填）
 *   unrecordedHead  排了、日子过了、一条出勤记录都没有 —— <b>该催的清单</b>
 *   absentHead      科室已确认的缺勤
 *   extraHead       没排班却来上班的
 * </pre>
 * 注意 presentHead + unrecordedHead + absentHead = planHead，缺勤和"没数"互不吞并。
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
