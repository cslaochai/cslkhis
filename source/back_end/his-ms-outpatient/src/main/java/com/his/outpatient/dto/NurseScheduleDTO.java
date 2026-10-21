package com.his.outpatient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import com.his.common.validation.AllowedValues;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 病区护理排班入参（外层空壳 + 内部静态类，同 InpatientLeaveDTO）。
 */
public class NurseScheduleDTO {

    /**
     * 周矩阵取数：wardId 必填，weekStart 空=本周（周一为界）
     */
    @Data
    public static class MatrixQuery {
        /**
         * 排班单元类型（1-病区 2-门诊科室；空按 1-病区）
         */
        private Integer unitType;
        /**
         * 排班单元ID（unitType=1 取病区ID，=2 取门诊科室ID）
         */
        @NotNull(message = "请选择排班单元")
        private Long wardId;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate weekStart;
    }

    /**
     * 点格排班/改格（一人一天一条，撞唯一键即覆盖原走向）
     */
    @Data
    public static class CellUpsert {
        /**
         * 排班单元类型（1-病区 2-门诊科室；空按 1-病区）
         */
        @AllowedValues(values = {"1", "2"}, message = "排班单元类型取值不合法")
        private Integer unitType;
        /**
         * 排班单元ID（unitType=1 取病区ID，=2 取门诊科室ID）
         */
        @NotNull(message = "请选择排班单元")
        private Long wardId;
        /**
         * 护士ID
         */
        @NotNull(message = "请选择护士")
        private Long employeeId;
        /**
         * 排班日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "排班日期不能为空")
        private LocalDate scheduleDate;
        /**
         * 状态（1-上班 2-休息 3-请假 4-培训 5-停班）
         */
        @NotNull(message = "请选择排班状态（上班/休息/请假/培训/停班）")
        @AllowedValues(values = {"1", "2", "3", "4", "5"}, message = "排班状态不合法")
        private Integer scheduleStatus;
        /**
         * 仅状态=1-上班时必填；非上班由服务端强制置空，不接受前端残留值
         */
        private Long shiftId;
        /**
         * 请假事由、调班说明等；长度由服务端截到列宽 500
         */
        private String remark;
    }

    /**
     * 批量铺班：共用「状态+班次」× 选中日期 × 多选护士，笛卡尔展开，只填空缺格、已排的跳过不覆盖。
     * 展开与落库在后端单事务内完成，前端禁止循环调用单格接口。
     */
    @Data
    public static class BatchUpsert {
        /** 排班单元类型（1-病区 2-门诊科室；空按 1-病区） */
        @AllowedValues(values = {"1", "2"}, message = "排班单元类型取值不合法")
        private Integer unitType;
        /** 排班单元ID（unitType=1 取病区ID，=2 取门诊科室ID） */
        @NotNull(message = "请选择排班单元")
        private Long wardId;
        /** 共用状态（1-上班 2-休息 3-请假 4-培训 5-停班） */
        @NotNull(message = "请选择排班状态（上班/休息/请假/培训/停班）")
        @AllowedValues(values = {"1", "2", "3", "4", "5"}, message = "排班状态不合法")
        private Integer scheduleStatus;
        /** 共用班次ID：仅状态=1-上班时必填，非上班由服务端置空 */
        private Long shiftId;
        /** 铺班日期列表（≤31 天，跨周/跨月分批） */
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotEmpty(message = "排班日期不能为空")
        @Size(max = 31, message = "一次最多铺 31 天，跨月请分批")
        private List<LocalDate> scheduleDates;
        /** 选中的护士（≤100 人） */
        @Valid
        @NotEmpty(message = "请选择排班护士")
        @Size(max = 100, message = "一次最多铺 100 位护士")
        private List<BatchEmployee> items;
        /** 统一备注/调班说明，服务端截到列宽 500 */
        private String remark;

        @Data
        public static class BatchEmployee {
            @NotNull(message = "请选择护士")
            private Long employeeId;
            /** 护士姓名（前端带上，报错点名用，服务端以员工档案为准复核） */
            private String nurseName;
        }
    }

    /**
     * 复制上周：只填目标周的空缺格，已排的一律不覆盖
     */
    @Data
    public static class CopyWeek {
        /**
         * 排班单元类型（1-病区 2-门诊科室；空按 1-病区）
         */
        @AllowedValues(values = {"1", "2"}, message = "排班单元类型取值不合法")
        private Integer unitType;
        /**
         * 排班单元ID（unitType=1 取病区ID，=2 取门诊科室ID）
         */
        @NotNull(message = "请选择排班单元")
        private Long wardId;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "请选择来源周")
        private LocalDate sourceWeekStart;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "请选择目标周")
        private LocalDate targetWeekStart;
    }

    /**
     * 规则校验（区间默认周矩阵那一周）
     */
    @Data
    public static class CheckQuery {
        /**
         * 排班单元类型（1-病区 2-门诊科室；空按 1-病区）
         */
        private Integer unitType;
        /**
         * 排班单元ID（unitType=1 取病区ID，=2 取门诊科室ID）
         */
        @NotNull(message = "请选择排班单元")
        private Long wardId;
        /**
         * 开始日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        /**
         * 结束日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }

    /**
     * 月度工时统计
     */
    @Data
    public static class WorkloadQuery {
        /**
         * 排班单元类型（1-病区 2-门诊科室；空按 1-病区）
         */
        private Integer unitType;
        /**
         * 排班单元ID（unitType=1 取病区ID，=2 取门诊科室ID）
         */
        @NotNull(message = "请选择排班单元")
        private Long wardId;
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式必须为 yyyy-MM")
        private String month;
    }

    /**
     * 台账分页（跨病区，护理部查排班历史用）
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class PageQuery extends PageParam {
        /**
         * 关键字
         */
        private String keyword;
        /**
         * 病区ID
         */
        private Long wardId;
        /**
         * 科室ID
         */
        private Long deptId;
        /**
         * 开始日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        /**
         * 结束日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
        /**
         * 状态（1-上班 2-休息 3-请假 4-培训 5-停班）
         */
        private Integer scheduleStatus;
    }

    /**
     * 人力配置标准保存（shiftId=0 的病区级行只认工时/连班/总人数，班次行只认人数）
     */
    @Data
    public static class RuleUpsert {
        /**
         * 主键ID
         */
        private Long id;
        /**
         * 排班单元类型（1-病区 2-门诊科室；空按 1-病区）
         */
        @AllowedValues(values = {"1", "2"}, message = "排班单元类型取值不合法")
        private Integer unitType;
        /**
         * 排班单元ID（unitType=1 取病区ID，=2 取门诊科室ID）
         */
        @NotNull(message = "请选择排班单元")
        private Long wardId;
        /**
         * 班次ID
         */
        @NotNull(message = "请选择班次（病区合计传 0）")
        private Long shiftId;
        @NotNull(message = "最低在岗人数不能为空")
        private Integer minStaff;
        /**
         * 0=不限（留空按 0 处理）
         */
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

}
