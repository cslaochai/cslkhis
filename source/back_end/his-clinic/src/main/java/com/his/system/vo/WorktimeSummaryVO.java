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

    /** 汇总日期 */
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

    /** 排了上班的人数 */
    @Schema(description = "排了上班的人数")
    private Integer planHead;

    /** 实际到岗人数 */
    @Schema(description = "实际到岗人数")
    private Integer presentHead;

    /** 已确认缺勤人数 */
    @Schema(description = "已确认缺勤人数")
    private Integer absentHead;

    /** 未回填人数（有排班无出勤记录，需催科室登记） */
    @Schema(description = "未回填人数（有排班无出勤记录，需催科室登记）")
    private Integer unrecordedHead;

    /** 无排班却来上班的人数 */
    @Schema(description = "无排班却来上班的人数")
    private Integer extraHead;

    /** 计划工时合计（分钟） */
    @Schema(description = "计划工时合计（分钟）")
    private Long plannedMinutes;

    /** 实际工时合计（分钟） */
    @Schema(description = "实际工时合计（分钟）")
    private Long actualMinutes;

    /** 工时差（分钟） */
    @Schema(description = "工时差（分钟）")
    private Long diffMinutes;

    /** 工时达成率（%） */
    @Schema(description = "工时达成率（%）")
    private BigDecimal fulfillRate;
}
