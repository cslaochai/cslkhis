package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 病区护理排班出参（内层静态类组织，同 InpatientLeaveVO）。
 *
 * <p><b>所有 Long 主键/外键一律字符串序列化</b>：雪花 19 位超出 JS Number.MAX_SAFE_INTEGER，
 * 裸数字回前端会丢精度，拿丢过的值回查就是「排班行不存在」。
 *
 * <p>日期与时间在 SQL 侧用 DATE_FORMAT/直接取 CHAR 别名字符串化，
 * 不在 Java 侧靠 Jackson 默认格式碰运气（AGENTS §3）。
 */
public class NurseScheduleVO {

    /**
     * 病区候选（排班页顶部的病区下拉：只列当前岗位可见科室下的病区）
     */
    @Data
    /**
     * 护理排班单元（sql/209 起不止病区一种）。
     *
     * <p>{@code unitType} 决定 {@code wardId} 这一列装的是谁的 id：
     * 1-病区 → {@code sys_ward.ward_id}；2-门诊科室 → {@code sys_department.id}。
     * 字段名沿用 wardId 是为了不让存量 1680 行和现有页面一起改一轮，
     * <b>判定一律以 unitType 为准</b>。
     */
    public static class Ward implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 排班单元类型（1-病区 2-门诊科室）
         */
        private Integer unitType;
        /**
         * 排班单元ID（unitType=1 时为病区ID，=2 时为门诊科室ID）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;
        private String wardCode;
        /**
         * 病区名称
         */
        private String wardName;
        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;
        /**
         * 该病区所属科室的在册护士数（0 人的病区排不了班，下拉里要看得见）
         */
        private Integer nurseCount;
    }

    /**
     * 护士候选（矩阵的行轴）
     */
    @Data
    public static class Nurse implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 护士ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
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
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;
    }

    /**
     * 护理班次（矩阵格子里可选的班次，use_scope=2 那一册）
     */
    @Data
    public static class ShiftOption implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 班次ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
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
        private Integer durationMinutes;
        /**
         * 是否夜班口径（开始时刻 &lt; 08:00 或 &ge; 16:00），连续夜班上限的判定依据
         */
        private Boolean night;
    }

    /**
     * 矩阵格子（一个人一天）
     */
    @Data
    public static class Cell implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 主键ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /**
         * 护士ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long employeeId;
        /**
         * 排班日期
         */
        private String scheduleDate;
        /**
         * 星期（1-周一 7-周日）
         */
        private Integer weekDay;
        /**
         * 班次ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
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
         * 状态（1-上班 2-休息 3-请假 4-培训 5-停班）
         */
        private Integer scheduleStatus;
        /**
         * 1-上班 2-休息 3-请假 4-培训 5-停班（服务端翻好的中文，前端不再抄一份码表）
         */
        private String scheduleStatusText;
        /**
         * 生成来源（1-手工 2-模板 3-复制周期 4-换班）
         */
        private Integer scheduleSource;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 排班台账行（跨病区回看：带病区/科室/工号等全快照列）
     */
    @Data
    public static class Row implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 主键ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;
        /**
         * 病区名称
         */
        private String wardName;
        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;
        /**
         * 排班日期
         */
        private String scheduleDate;
        /**
         * 星期（1-周一 7-周日）
         */
        private Integer weekDay;
        /**
         * 护士ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
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
        @JsonSerialize(using = ToStringSerializer.class)
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
         * 状态（1-上班 2-休息 3-请假 4-培训 5-停班）
         */
        private Integer scheduleStatus;
        private String scheduleStatusText;
        /**
         * 生成来源（1-手工 2-模板 3-复制周期 4-换班）
         */
        private Integer scheduleSource;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 周矩阵：行=护士、列=周一到周日，cells 平铺由前端按 employeeId|scheduleDate 建索引
     */
    @Data
    public static class Matrix implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;
        /**
         * 病区名称
         */
        private String wardName;
        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;
        private String weekStart;
        private String weekEnd;
        /**
         * 7 天日期（yyyy-MM-dd，含周日）
         */
        private List<String> days;
        private List<Nurse> nurses;
        private List<ShiftOption> shifts;
        private List<Cell> cells;
        /**
         * 本周规则校验告警（不阻断保存，只列出来）
         */
        private List<Warning> warnings;
        /**
         * 本周每日每班次在岗人数（矩阵底部「今日 N 人 / 应 M 人」用）
         */
        private List<Staffing> staffing;
    }

    /**
     * 某日某班次在岗人数（shiftId=0 表示全病区合计）
     */
    @Data
    public static class Staffing implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 排班日期
         */
        private String scheduleDate;
        /**
         * 班次ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long shiftId;
        /**
         * 班次名称
         */
        private String shiftName;
        private Integer staffCount;
        private Integer minStaff;
        private Integer maxStaff;
    }

    /**
     * 单条规则校验告警
     */
    @Data
    public static class Warning implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 1-告警（违反人力/工时标准，必须处置）2-提示（如未排班空缺）
         */
        private Integer level;
        /**
         * STAFF_GAP / STAFF_OVER / WEEK_HOURS / NIGHT_STREAK / WORK_STREAK / SHIFT_ON_NON_WORK / NOT_SCHEDULED
         */
        private String type;
        /**
         * 排班日期
         */
        private String scheduleDate;
        /**
         * 班次ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long shiftId;
        /**
         * 班次名称
         */
        private String shiftName;
        /**
         * 护士ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long employeeId;
        /**
         * 护士姓名
         */
        private String nurseName;
        private Integer actual;
        private Integer required;
        /**
         * 消息内容
         */
        private String message;
    }

    /**
     * 区间规则校验结果
     */
    @Data
    public static class CheckResult implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;
        /**
         * 病区名称
         */
        private String wardName;
        /**
         * 开始日期
         */
        private String startDate;
        /**
         * 结束日期
         */
        private String endDate;
        private Integer alertCount;
        private Integer hintCount;
        private List<Warning> warnings;
    }

    /**
     * 工时统计行（一个月一个护士一行）
     */
    @Data
    public static class Workload implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 护士ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
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
        private Integer workDays;
        private Integer restDays;
        private Integer leaveDays;
        private Integer trainingDays;
        private Integer suspendedDays;
        private Integer nightDays;
        /**
         * 工时
         */
        private Integer workMinutes;
        private BigDecimal workHours;
        /**
         * 已写排班行的天数（区间内该人有走向的日子）
         */
        private Integer writtenDays;
        /**
         * 缺排班天数 = 区间天数 - writtenDays，用于发现「整周没排」
         */
        private Integer missingDays;
    }

    /**
     * 月度工时结果（含病区合计，护理部月报直接抄这组数）
     */
    @Data
    public static class MonthWorkload implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;
        /**
         * 病区名称
         */
        private String wardName;
        private String month;
        private Integer nurseCount;
        private Integer totalWorkDays;
        private Integer totalNightDays;
        private BigDecimal totalWorkHours;
        /**
         * 单周工时上限（来自病区规则，0/空=未配置），前端在超限行上标色
         */
        private BigDecimal maxWeekHours;
        private List<Workload> rows;
    }

    /**
     * 人力配置标准行
     */
    @Data
    public static class Rule implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 主键ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;
        /**
         * 病区名称
         */
        private String wardName;
        /**
         * 班次ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long shiftId;
        /**
         * 班次名称
         */
        private String shiftName;
        private Integer minStaff;
        private Integer maxStaff;
        private BigDecimal maxWeekHours;
        private Integer maxConsecutiveNightDays;
        private Integer maxConsecutiveWorkDays;
        private Integer status;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 点格保存结果：回写该行 + 当日相关告警，让用户立刻看见「这么排会缺人」
     */
    @Data
    public static class SaveResult implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 主键ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /**
         * 排班日期
         */
        private String scheduleDate;
        /**
         * 班次名称
         */
        private String shiftName;
        /**
         * 状态（1-上班 2-休息 3-请假 4-培训 5-停班）
         */
        private Integer scheduleStatus;
        private BigDecimal weekHours;
        private List<Warning> warnings;
    }

    /**
     * 复制上周结果
     */
    @Data
    public static class CopyResult implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer copiedCount;
        private Integer skippedCount;
        /**
         * 消息内容
         */
        private String message;
    }

    /**
     * 删除影响提示（只回数字，前端弹确认后自己决定要不要重试）
     */
    @Data
    public static class DeleteResult implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 主键ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /**
         * 护士姓名
         */
        private String nurseName;
        /**
         * 排班日期
         */
        private String scheduleDate;
    }
}
