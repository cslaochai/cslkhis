package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 全院岗位排班（谁 · 哪天 · 在哪个单元 · 什么班 · 出不出勤）—— 排班事实的唯一载体。
 *
 * <p><b>为什么需要它</b>：出诊计划的状态说的是「这批号还能不能挂」，护理那条线的状态说的是
 * 「这个人今天来不来」，值守那条线说的是「这个责任位当天归谁」。三件事原先各存一套，
 * 于是同一个人可以在两张表里被排到重叠的时间段都不报错，而「今日在岗」只能靠号源状态猜。
 * 本表把「人与时间」这一维收回来，号源、工时、责任位仍留在各自的扩展对象上。
 *
 * <p>两条口径：
 * <ol>
 *   <li><b>非上班也占一行</b>：休息/请假/培训/停班写成事实，周矩阵才看得出走向，
 *       「连上六天」「请假还排班」才有依据；此时无班次，班次位置用 0 表达（0 = 无班次）。</li>
 *   <li><b>跨零点班归开始日</b>：21:00~次日 08:00 记在 21:00 那天，
 *       所以按时刻查在岗必须同时捞「今天」与「昨天」两天的行。</li>
 * </ol>
 *
 * <p>起止时间是班次的快照（班次改了时限不影响已排行，历史可追）；
 * 工时由班次带出，前端传什么都不认。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_staff_schedule")
public class BizStaffSchedule extends BaseEntity {

    /**
     * 排班日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /**
     * 星期（1-周一 7-周日）
     */
    private Integer weekDay;

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    private Integer orgType;

    /**
     * 排班单元ID（全院级为 0）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /**
     * 排班单元名称（快照）
     */
    private String orgName;

    /**
     * 科室ID（全院级为 0）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称（快照）
     */
    private String deptName;

    /**
     * 排班对象（员工）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 工号（快照）
     */
    private String empCode;

    /**
     * 姓名（快照）
     */
    private String employeeName;

    /**
     * 所依据的岗位（人 × 科室 × 角色）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeePostId;

    /**
     * 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）
     */
    private Integer staffType;

    /**
     * 标准班次ID（0-无班次）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 开始时间 HH:mm（班次快照）
     */
    private String startTime;

    /**
     * 结束时间 HH:mm（班次快照，早于开始时间属次日）
     */
    private String endTime;

    /**
     * 出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班）
     */
    private Integer dutyStatus;

    /**
     * 响应形态（1-坐班 2-听班 3-留院值班）
     */
    private Integer attendMode;

    /**
     * 是否出诊（0-否 1-是）
     */
    private Integer clinicFlag;

    /**
     * 工时（分钟）
     */
    private Integer workMinutes;

    /**
     * 生成来源（1-手工 2-模板 3-复制周期 4-换班）
     */
    private Integer scheduleSource;

    /**
     * 来源排班周模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;
}
