package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 病区护理排班行（病区护理排班，sql/166）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nurse_schedule")
public class BizNurseSchedule extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 病区ID（兼容列：unit_type=1 时等于 unit_id；=2 门诊场景时冗余写科室ID，供台账/渲染继续用）
     */
    private Long wardId;
    /**
     * 病区名称（快照，门诊场景写科室名）
     */
    private String wardName;
    /**
     * 病区所属科室：数据范围（岗位可见科室）按它收口
     */
    private Long deptId;
    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 排班单元类型（sql/209）：1-病区 2-门诊科室。
     *
     * <p>护理排班原先只有病区一种单元，门诊科室的护士（分诊、跟诊、治疗处置）
     * 在系统里没有落脚点。{@code wardId} 那一列装什么由这个值决定，
     * 所以读 {@code wardId} 之前必须先读它。
     */
    private Integer unitType;

    /**
     * 排班单元ID（unit_type=1 → sys_ward.ward_id；=2 → sys_department.id）
     */
    private Long unitId;

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;
    /**
     * 1-周一 ... 7-周日，冗余给矩阵渲染
     */
    private Integer weekDay;

    /**
     * 护士ID
     */
    private Long employeeId;
    /**
     * 工号
     */
    private String empCode;
    /**
     * 护士姓名
     */
    private String nurseName;
    /**
     * 职称
     */
    private String nurseTitle;

    /**
     * 班次ID
     */
    private Long shiftId;
    /**
     * 班次名称
     */
    private String shiftName;
    /**
     * 开始时间 HH
     */
    private String startTime;
    /**
     * 结束时间 HH
     */
    private String endTime;
    /**
     * 工时
     */
    private Integer workMinutes;
    /**
     * StaffDutyStatusEnum：1-上班 2-休息 3-请假 4-培训 5-停班
     */
    private Integer scheduleStatus;
    /**
     * 生成来源（1-手工 2-模板 3-复制周期 4-换班）
     */
    private Integer scheduleSource;

    /**
     * 这条护理格子对应的出勤事实 {@code biz_staff_schedule.id}（sql/205）。
     *
     * <p>护理格子与全院出勤底座是<b>同一件事的两张皮</b>：格子回答"这个护士这天在这个病区上什么班"，
     * 底座回答"这个人这天在什么单元处于什么在岗状态"。底座是互斥判定的唯一场所，
     * 所以必须由格子这边回指过去，改动才能双向追溯。
     *
     * <p><b>只是指路牌，不是外键</b>：底座行可以被「全院岗位排班」直接物理删掉，
     * 加外键会让那边删不动、这边留下指向空白的悬空引用。悬空由
     * {@code StaffScheduleSourceEnum} 相关的自检项（sql/205 V3）盯着，不由数据库约束盯着。
     */
    private Long staffScheduleId;
}
