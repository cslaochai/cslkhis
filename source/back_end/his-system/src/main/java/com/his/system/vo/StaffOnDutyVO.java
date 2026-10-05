package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 此刻在岗人员出参（「现在谁在班上」）。
 *
 * <p>与「今天排了谁」是两件事：凌晨两点在岗的是昨天夜班的人，今日名单里没有他。
 */
@Data
public class StaffOnDutyVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    private String empCode;
    private String employeeName;

    private Integer staffType;
    private String staffTypeName;

    private Integer orgType;
    private String orgTypeText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    private String orgName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    private String shiftName;

    private String startTime;
    private String endTime;

    private Integer attendMode;
    private String attendModeText;

    /**
     * 是否出诊（0-否 1-是）
     */
    private Integer clinicFlag;

    /**
     * 排班日期（跨零点班归开始日，所以可能是昨天）
     */
    private String scheduleDate;
}
