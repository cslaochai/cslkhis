package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 值守点位出参。
 */
@Data
public class DutyPostVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String postCode;
    private String postName;

    private Integer dutyScope;
    private String dutyScopeText;

    private Integer orgType;
    private String orgTypeText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    private String orgName;

    private Integer roleType;
    private String roleTypeText;

    /**
     * 值班层级（0-不适用 1-一线 2-二线 3-三线）
     */
    private Integer dutyLevel;
    private String dutyLevelText;

    /**
     * 响应形态（1-坐班 2-听班 3-留院值班）
     */
    private Integer attendMode;
    private String attendModeText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    private String shiftName;

    /**
     * 班次开始时间 HH:mm
     */
    private String startTime;

    /**
     * 班次结束时间 HH:mm
     */
    private String endTime;

    private Integer requiredStaffType;
    private String requiredStaffTypeName;

    private String phone;
    private Integer sortNo;
    private Integer status;
    private String remark;
}
