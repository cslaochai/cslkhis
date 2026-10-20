package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 差异归因 → 编制校准建议（聚合内联于 BizStaffAttendanceMapper.xml 的 adviceAgg）。
 */
@Data
@Schema(description = "编制校准建议")
public class CalibrationAdviceVO {

    /** 排班单元类型（1-科室 2-病区 3-全院） */
    private Integer orgType;

    /** 单元ID：科室或病区标识，全院口径为空 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /** 单元名称：编制建议归属的科室/病区名称 */
    private String orgName;

    /** 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他 7-护工护理辅助） */
    private Integer staffType;

    /** 统计覆盖的天数：本次归因取数的考勤天数 */
    @Schema(description = "统计覆盖的天数")
    private Integer daysCovered;

    /** 应出勤人次（人天）：同期排班计划折算的人力需求 */
    @Schema(description = "应出勤人次（人天）")
    private Long planHeadDays;

    /** 实到人次：有考勤回填的出勤人天 */
    @Schema(description = "实到人次")
    private Long presentHeadDays;

    /** 未回填人次：排班了但考勤缺失的人天，归因的不确定部分 */
    @Schema(description = "未回填人次")
    private Long unrecordedHeadDays;

    /** 已确认缺勤人次：请假等明确缺勤的人天 */
    @Schema(description = "已确认缺勤人次")
    private Long absentHeadDays;

    /** 无排班出勤人次（加班/支援）：未排班却到岗的人天 */
    @Schema(description = "无排班出勤人次（加班/支援）")
    private Long extraHeadDays;

    /** 计划工时合计（分钟） */
    @Schema(description = "计划工时合计（分钟）")
    private Long plannedMinutes;

    /** 实际工时合计（分钟） */
    @Schema(description = "实际工时合计（分钟）")
    private Long actualMinutes;

    /** 工时差（分钟）：实际减计划，负值说明人力缺口 */
    @Schema(description = "工时差（分钟）")
    private Long diffMinutes;

    /** 同期平均需求人数：按最低编制规则折算的在岗人数均值 */
    @Schema(description = "同期平均需求人数")
    private BigDecimal avgRequired;

    /** 同期平均实到人数 */
    @Schema(description = "同期平均实到人数")
    private BigDecimal avgPresent;

    /** 未回填占比（%）：考勤缺失度，过高时建议不可信 */
    @Schema(description = "未回填占比（%）")
    private BigDecimal unrecordedRate;

    /** 建议类型（1-上调 2-维持 3-核减 4-数据不足） */
    @Schema(description = "建议类型（1-上调 2-维持 3-核减 4-数据不足）")
    private Integer adviceType;

    /** 建议依据：给护理部/人事看的一句话结论 */
    @Schema(description = "建议依据（写给人看的一句话）")
    private String adviceText;

    /** 建议的编制下限（人）：仅建议类型为上调时有值，仍须护理部确认 */
    @Schema(description = "建议的编制下限（仅 adviceType=1 时有值，仍须护理部确认）")
    private Integer suggestMinStaff;
}
