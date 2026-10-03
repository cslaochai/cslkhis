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

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /** 星期（1-周一 7-周日） */
    private Integer weekDay;

    /** 星期文案 */
    private String weekDayText;

    private Integer orgType;
    private String orgTypeText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    private String orgName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    private String empCode;
    private String employeeName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeePostId;

    private Integer staffType;
    private String staffTypeName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /** 班次名称（班次已删除时为空，前端按「未分类」渲染） */
    private String shiftName;

    private String startTime;
    private String endTime;

    /** 是否跨零点班（结束时间早于开始时间） */
    private Boolean crossDay;

    private Integer dutyStatus;
    private String dutyStatusText;

    private Integer attendMode;
    private String attendModeText;

    /** 是否出诊（0-否 1-是） */
    private Integer clinicFlag;

    private Integer workMinutes;

    /** 工时（小时，一位小数字符串，避免前端再算一遍除法） */
    private String workHours;

    private Integer scheduleSource;
    private String scheduleSourceText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    private String remark;
}
