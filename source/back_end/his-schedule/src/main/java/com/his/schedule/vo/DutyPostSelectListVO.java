package com.his.schedule.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 值守点位下拉出参。
 */
@Data
public class DutyPostSelectListVO {

    /** 值守点位ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 点位编码 */
    private String postCode;
    /** 点位名称 */
    private String postName;

    /** 责任范围（1-全院行政 2-急诊 3-感染 4-总务 5-信息 6-临床科室） */
    private Integer dutyScope;
    /** 责任范围文案（由枚举 label 渲染） */
    private String dutyScopeText;

    /** 班内角色（1-主班 2-副班） */
    private Integer roleType;
    /** 班内角色文案（由枚举 label 渲染） */
    private String roleTypeText;

    /**
     * 值班层级（0-不适用 1-一线 2-二线 3-三线）
     */
    private Integer dutyLevel;
    /** 值班层级文案（由枚举 label 渲染） */
    private String dutyLevelText;

    /**
     * 响应形态（1-坐班 2-听班 3-留院值班）
     */
    private Integer attendMode;
    /** 响应形态文案（由枚举 label 渲染） */
    private String attendModeText;

    /** 标准班次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /** 标准班次名称 */
    private String shiftName;

    /** 班次开始时间 HH:mm */
    private String startTime;
    /** 班次结束时间 HH:mm（跨零点班次为次日时刻） */
    private String endTime;

    /**
     * 应到岗位类别（空=不限）
     */
    private Integer requiredStaffType;

    /**
     * 点位值班电话
     */
    private String phone;
}
