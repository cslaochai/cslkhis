package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实际出勤（某个人 · 某天 · 某个单元 · 某个班次，实际干了多久）。
 *
 * <p><b>它在闭环里的位置</b>：标准是「配多少人的规矩」、需求是「今天该来几个人」、
 * 排班是「安排了谁来」、本表是「<b>实际开展了没有</b>」。前三步 P0~P2 已经打通，
 * 缺了这一步，"缺 2 人"这句话永远只是计划层面的算术 —— 说不清是人没来，还是编制压根不够。
 *
 * <p><b>一条必须守住的界限：系统不许自己判缺勤。</b>
 * 「排了班但没有任何出勤记录」在本系统里只能叫<b>未回填</b>（差异类型 7），不能叫缺勤 ——
 * 人可能调班了、可能去支援别的单元了、可能打卡机坏了。缺勤（{@link #attendanceStatus} = 4）
 * 永远只能由科室/护士长显式确认产生，或由考勤机导入明确给出。
 * 这条线一破，工时和绩效的数据就全是猜出来的，而且没人会发现自己在猜。
 *
 * <p>{@link #staffScheduleId} 沿用项目已有的约定：<b>指路牌，不是外键</b> ——
 * 门诊/护理/值守三条线共享底座事实，真加外键会让其中一条线的删改牵连别人。
 *
 * <p>关于唯一键为什么不是简单的「人 × 日」：同一个人同一天可以在两个单元出勤
 * （上午本科室、下午去别的病区支援），也可以在同一个单元上两个班次（主班 + 加班）。
 * 这三件事在业务上是三条事实，不能被互相吃掉，所以收口到「人 × 日 × 单元 × 班次」，
 * 与底座 {@code biz_staff_schedule} 的 uk_emp_date_shift 同一族口径。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_staff_attendance")
public class BizStaffAttendance extends BaseEntity {

    /**
     * 关联的排班事实ID（biz_staff_schedule.id）；空=无计划的出勤（加班/支援/替班）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    /**
     * 员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 姓名
     */
    private String employeeName;

    /**
     * 工号
     */
    private String empCode;

    /**
     * 出勤日期（归属哪一天；夜班签退跨到次日也算这天）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /**
     * 实际出勤单元类型（1-科室 2-病区 3-全院）
     */
    private Integer orgType;

    /**
     * 实际出勤单元ID（全院级为 0）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /**
     * 单元名称
     */
    private String orgName;

    /**
     * 班次ID（0-无班次，如自由工时的加班）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政）
     */
    private Integer staffType;

    /**
     * 签到时间（NULL=没签到：缺勤确认或手工登记的工时）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkIn;

    /**
     * 签退时间（NULL=还没签退或缺勤）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOut;

    /**
     * 实际工时（分钟）：打卡则算，无打卡由科室确认后手工填
     */
    private Integer actualMinutes;

    /**
     * 计划工时（分钟）：biz_staff_schedule.work_minutes 的快照
     */
    private Integer plannedMinutes;

    /**
     * 超时工时（分钟）：GREATEST(0, 实际 - 计划)
     */
    private Integer overtimeMinutes;

    /**
     * 出勤状态（1-正常 2-迟到 3-早退 4-缺勤 5-替班 6-加班 7-支援）
     */
    private Integer attendanceStatus;

    /**
     * 替了谁的班（employee_id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long substituteFor;

    /**
     * 科室确认（0-待确认 1-已确认 2-有异议）
     */
    private Integer confirmStatus;

    /**
     * 确认人
     */
    private String confirmBy;

    /**
     * 确认时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;

    /**
     * 数据来源（1-人工登记 2-考勤机导入 3-系统判定）
     */
    private Integer dataSource;

    /**
     * 状态（0-停用 1-生效）
     */
    private Integer status;
}
