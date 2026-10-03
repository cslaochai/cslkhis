package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 值守点位下拉出参。
 */
@Data
public class DutyPostSelectListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String postCode;
    private String postName;

    private Integer dutyScope;
    private String dutyScopeText;

    private Integer roleType;
    private String roleTypeText;

    /** 值班层级（0-不适用 1-一线 2-二线 3-三线） */
    private Integer dutyLevel;
    private String dutyLevelText;

    /** 响应形态（1-坐班 2-听班 3-留院值班） */
    private Integer attendMode;
    private String attendModeText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    private String shiftName;

    private String startTime;
    private String endTime;

    /** 应到岗位类别（空=不限） */
    private Integer requiredStaffType;

    /** 点位值班电话 */
    private String phone;
}
