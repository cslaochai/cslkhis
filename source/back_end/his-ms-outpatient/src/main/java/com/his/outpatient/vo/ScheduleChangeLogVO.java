package com.his.outpatient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班变更留痕出参。
 */
@Data
public class ScheduleChangeLogVO {

    /** 变更留痕ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 员工排班ID：本次变更落在哪一行排班 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    /** 排班日期（关联 biz_schedule 快照，全局台账展示用） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /** 排班单元类型（1-科室 2-病区 3-全院） */
    private Integer orgType;

    /** 排班单元名称 */
    private String orgName;

    /** 变更类型（1-换班 2-代班 3-停班 4-加号 5-减号 6-出诊变更） */
    private Integer actionType;
    /** 变更类型文案（由枚举 label 渲染） */
    private String actionTypeText;

    /** 原值班人ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromEmployeeId;

    /**
     * 原值班人姓名（按留痕时的人快照取，人已被删时为空）
     */
    private String fromEmployeeName;

    /** 变更后承接的值班人ID（停班等无承接人的变更为空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toEmployeeId;

    /** 变更后值班人姓名 */
    private String toEmployeeName;

    /** 原班次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromShiftId;

    /** 原班次名称 */
    private String fromShiftName;

    /** 新班次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toShiftId;

    /** 新班次名称 */
    private String toShiftName;

    /** 变更数量（加号/减号类变更的号源条数） */
    private Integer amount;
    /** 变更原因 */
    private String reason;

    /** 变更发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /** 创建人 */
    private String createBy;
}
