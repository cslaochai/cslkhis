package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 差异归因 → 编制校准建议（对应视图 {@code v_staff_calibration_advice}）。
 *
 * <p><b>闭环能转起来的标志不是"出了张报表"，是执行结果能改下一轮的编制标准。</b>
 * 连着两个人靠加班顶班 → 说明编制不够 → 建议上调 {@code min_staff}；
 * 长期实际到岗远高于需求又没事发生 → 说明编制虚高 → 建议核减。
 *
 * <p>这里只给建议，<b>不自动改 {@code biz_staff_plan_rule}</b>：编制是护理部的权，
 * 系统给数、人做决定。这条边界不能越 —— 一旦系统自动加人，
 * 排班员就再也不会去解释"为什么这个病区突然多了两个人"。
 *
 * <p>{@code adviceType}：1-建议上调 2-编制合适 3-建议核减 <b>4-数据不足</b>。
 * 最后一档最重要：过去 14 天"未回填"超过 30% 时，任何上调/核减的结论都是
 * 拿半个事实糊弄人 —— 直接说不知道，并把"请先补 N 条出勤记录"写进建议里。
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
