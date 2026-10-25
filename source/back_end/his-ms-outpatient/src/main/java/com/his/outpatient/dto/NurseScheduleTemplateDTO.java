package com.his.outpatient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import com.his.common.validation.AllowedValues;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 护理排班模板入参（外层空壳 + 内部静态类，同 NurseScheduleDTO）。
 */
public class NurseScheduleTemplateDTO {

    /**
     * 模板新增/修改（合一入口，按 id 有无分支）
     */
    @Data
    public static class Upsert {
        /** 主键ID（空=新增） */
        private Long id;

        /** 模板名称 */
        @NotNull(message = "请填写模板名称")
        private String name;

        /** 排班单元类型（1-病区 2-门诊科室；空按 1-病区） */
        @AllowedValues(values = {"1", "2"}, message = "排班单元类型取值不合法")
        private Integer unitType;

        /** 排班单元ID（unitType=1 取病区ID，=2 取门诊科室ID） */
        @NotNull(message = "请选择排班单元")
        private Long wardId;

        /** 单双周（0-每周 1-单周 2-双周） */
        private Integer weekParity;

        /** 生效起始日期（空=不限） */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate validFrom;

        /** 生效截止日期（空=不限） */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate validUntil;

        /** 共享范围（0-个人 1-权限组共享） */
        private Integer scope;

        /** 状态（0-停用 1-启用） */
        private Integer status;

        /** 备注 */
        private String remark;

        /** 明细行（至少一行） */
        @Valid
        @NotEmpty(message = "模板至少要有一行明细")
        private List<Item> items;
    }

    /**
     * 模板明细行：一个护士在某星期几的出勤
     */
    @Data
    public static class Item {
        /** 护士ID */
        @NotNull(message = "请选择护士")
        private Long employeeId;

        /** 护士姓名（前端带上，报错点名用，服务端以员工档案为准复核） */
        private String employeeName;

        /** 星期几（1-周一 … 7-周日） */
        @NotNull(message = "请选择星期")
        @AllowedValues(values = {"1", "2", "3", "4", "5", "6", "7"}, message = "星期取值不合法")
        private Integer weekDay;

        /** 出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班） */
        @NotNull(message = "请选择出勤状态")
        @AllowedValues(values = {"1", "2", "3", "4", "5"}, message = "出勤状态不合法")
        private Integer scheduleStatus;

        /** 班次ID：仅状态=1-上班时有效，非上班由服务端强制置空 */
        private Long shiftId;

        /** 备注 */
        private String remark;
    }

    /**
     * 模板分页查询（关键词=名称模糊；按单元/星期/状态/共享范围过滤）
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class PageQuery extends PageParam {
        /** 关键词（模板名称模糊） */
        private String keyword;
        /** 排班单元类型（1-病区 2-门诊科室） */
        private Integer unitType;
        /** 排班单元ID */
        private Long wardId;
        /** 星期几（1-7） */
        private Integer weekDay;
        /** 状态（0-停用 1-启用） */
        private Integer status;
        /** 共享范围（0-个人 1-权限组共享） */
        private Integer scope;
    }

    /**
     * 适用记录分页查询
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ApplyPageQuery extends PageParam {
        /** 套用模板ID（空=全部模板的适用记录） */
        private Long templateId;
        /** 排班单元类型（1-病区 2-门诊科室） */
        private Integer unitType;
        /** 排班单元ID */
        private Long wardId;
    }

    /**
     * 套用模板到目标周：复用现有护理周排班底座（staffScheduleService.replaceDayAttendance，来源=TEMPLATE）
     */
    @Data
    public static class Apply {
        /** 套用模板ID */
        @NotNull(message = "请选择套用模板")
        private Long templateId;

        /** 排班单元类型（1-病区 2-门诊科室；空按 1-病区） */
        @AllowedValues(values = {"1", "2"}, message = "排班单元类型取值不合法")
        private Integer unitType;

        /** 套用单元ID（决定把模板落到哪个病区/门诊科室的周排班；默认取模板所属单元） */
        @NotNull(message = "请选择套用单元")
        private Long wardId;

        /** 目标周起始（周一；空=下周） */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate targetWeekStart;
    }

    /**
     * 模板规则（班次实用排班模式）新增/修改
     */
    @Data
    public static class RuleUpsert {
        /** 主键ID */
        private Long id;
        /** 模式名称（如「APN 三班倒」） */
        @NotNull(message = "请填写模式名称")
        private String ruleName;
        /** 模式编码（APN / 8-8 / GROUP，业务约定） */
        @NotNull(message = "请填写模式编码")
        private String ruleCode;
        /** 班次模式明细（JSON，前端只读展示） */
        private String patternDetail;
        /** 状态（0-停用 1-启用） */
        private Integer status;
        /** 备注 */
        private String remark;
    }
}
