package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 差异归因 → 编制校准建议（对应视图 v_staff_calibration_advice）。
 */
@Data
@Schema(description = "编制校准建议")
public class CalibrationAdviceVO {

    private Integer orgType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    private String orgName;

    private Integer staffType;

    @Schema(description = "统计覆盖的天数")
    private Integer daysCovered;

    @Schema(description = "应出勤人次（人天）")
    private Long planHeadDays;

    @Schema(description = "实到人次")
    private Long presentHeadDays;

    @Schema(description = "未回填人次")
    private Long unrecordedHeadDays;

    @Schema(description = "已确认缺勤人次")
    private Long absentHeadDays;

    @Schema(description = "无排班出勤人次（加班/支援）")
    private Long extraHeadDays;

    @Schema(description = "计划工时合计（分钟）")
    private Long plannedMinutes;

    @Schema(description = "实际工时合计（分钟）")
    private Long actualMinutes;

    @Schema(description = "工时差（分钟）")
    private Long diffMinutes;

    @Schema(description = "同期平均需求人数")
    private BigDecimal avgRequired;

    @Schema(description = "同期平均实到人数")
    private BigDecimal avgPresent;

    @Schema(description = "未回填占比（%）")
    private BigDecimal unrecordedRate;

    @Schema(description = "建议类型（1-上调 2-维持 3-核减 4-数据不足）")
    private Integer adviceType;

    @Schema(description = "建议依据（写给人看的一句话）")
    private String adviceText;

    @Schema(description = "建议的编制下限（仅 adviceType=1 时有值，仍须护理部确认）")
    private Integer suggestMinStaff;
}
