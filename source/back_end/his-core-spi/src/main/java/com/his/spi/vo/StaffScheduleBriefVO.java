package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 岗位排班摘要：跨模块要确认「这个人这天到底出不出诊、现在这个班谁在值」时的出参。
 *
 * <p>一个类承载多种取数口径，各口径只填自己读的列，未填即为 null；
 * 排班的录入、审核、发布编排仍归排班域自己。
 */
@Data
@NoArgsConstructor
public class StaffScheduleBriefVO {

    /**
     * 排班记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;

    /**
     * 排班对象（员工）ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 员工姓名快照
     */
    private String employeeName;

    /**
     * 标准班次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 班次开始时间 HH:mm
     */
    private String startTime;

    /**
     * 班次结束时间 HH:mm（早于开始时间属次日）
     */
    private String endTime;

    /**
     * 岗位类别，取值见 StaffTypeEnum
     */
    private Integer staffType;

    /**
     * 出勤状态，取值见 StaffDutyStatusEnum
     */
    private Integer dutyStatus;

    /**
     * 响应形态，取值见 AttendModeEnum
     */
    private Integer attendMode;

    /**
     * 是否出诊（0-否 1-是）
     */
    private Integer clinicFlag;

    /**
     * 是否已签到到岗（true=实际在岗；false/null=仅计划在岗）。
     * 仅值班命中口径填充。
     */
    private Boolean actualOnDuty;
}
