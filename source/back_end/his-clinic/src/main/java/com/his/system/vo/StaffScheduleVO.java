package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 全院岗位排班列表出参（含各码值的中文口径，前端不再自己翻译）。
 */
@Data
public class StaffScheduleVO {

    /** 排班记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 排班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /**
     * 星期（1-周一 7-周日）
     */
    private Integer weekDay;

    /**
     * 星期文案
     */
    private String weekDayText;

    /** 排班单元类型（1-科室 2-病区 3-全院） */
    private Integer orgType;
    /** 排班单元类型文案（由枚举 label 渲染） */
    private String orgTypeText;

    /** 排班单元ID（全院级为 0） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /** 排班单元名称 */
    private String orgName;

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称 */
    private String deptName;

    /** 员工ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /** 工号 */
    private String empCode;
    /** 员工姓名 */
    private String employeeName;

    /** 所依据的岗位（角色 × 科室） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeePostId;

    /** 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他 7-护工） */
    private Integer staffType;
    /** 岗位类别文案（由枚举 label 渲染） */
    private String staffTypeName;

    /** 班次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 班次名称（班次已删除时为空，前端按「未分类」渲染）
     */
    private String shiftName;

    /** 开始时间 HH:mm（班次快照） */
    private String startTime;
    /** 结束时间 HH:mm（班次快照，早于开始时间属次日） */
    private String endTime;

    /**
     * 是否跨零点班（结束时间早于开始时间）
     */
    private Boolean crossDay;

    /** 出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班） */
    private Integer dutyStatus;
    /** 出勤状态文案（由枚举 label 渲染） */
    private String dutyStatusText;

    /** 响应形态（1-坐班 2-听班 3-留院值班） */
    private Integer attendMode;
    /** 响应形态文案（由枚举 label 渲染） */
    private String attendModeText;

    /**
     * 是否出诊（0-否 1-是）
     */
    private Integer clinicFlag;

    /** 工时（分钟） */
    private Integer workMinutes;

    /**
     * 工时（小时，一位小数字符串，避免前端再算一遍除法）
     */
    private String workHours;

    /** 排班来源（1-手工 2-模板 3-复制周期 4-换班） */
    private Integer scheduleSource;
    /** 排班来源文案（由枚举 label 渲染） */
    private String scheduleSourceText;

    /** 来源排班周模板ID（非模板生成时为空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /** 备注 */
    private String remark;
}
