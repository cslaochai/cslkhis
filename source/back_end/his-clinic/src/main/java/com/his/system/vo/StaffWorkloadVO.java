package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 员工排班月度工时统计结果（全区岗位通用）。
 * 一人一行，整段未排班的人也在名单内（missingDays 能看出谁被漏排）。
 */
@Data
public class StaffWorkloadVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计月份 */
    private String month;
    /** 排班单元类型（空=全部） */
    private Integer orgType;
    /** 排班单元ID（空=全部） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;
    /** 排班单元名称 */
    private String orgName;
    /** 岗位类别（空=全部） */
    private Integer staffType;
    /** 统计人数 */
    private Integer staffCount;
    /** 总上班天数 */
    private Integer totalWorkDays;
    /** 总夜班天数 */
    private Integer totalNightDays;
    /** 总工时（小时） */
    private BigDecimal totalWorkHours;
    /** 明细行（按工号排序） */
    private List<Workload> rows;

    /**
     * 单人工时统计行
     */
    @Data
    public static class Workload implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 员工ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long employeeId;
        /** 工号 */
        private String empCode;
        /** 姓名 */
        private String employeeName;
        /** 岗位类别 */
        private Integer staffType;
        /** 岗位类别文案 */
        private String staffTypeName;
        /** 上班天数 */
        private Integer workDays;
        /** 休息天数 */
        private Integer restDays;
        /** 请假天数 */
        private Integer leaveDays;
        /** 培训天数 */
        private Integer trainingDays;
        /** 停班天数 */
        private Integer suspendedDays;
        /** 夜班天数 */
        private Integer nightDays;
        /** 工时（分钟） */
        private Integer workMinutes;
        /** 工时（小时，保留一位） */
        private BigDecimal workHours;
        /** 已写排班行的天数（区间内该人有走向的日子） */
        private Integer writtenDays;
        /** 缺排班天数 = 区间天数 - writtenDays，用于发现「整月没排」 */
        private Integer missingDays;
    }
}
