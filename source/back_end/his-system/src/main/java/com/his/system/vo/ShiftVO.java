package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 班次字典 VO（下拉/管理共用）
 */
@Data
public class ShiftVO {

    /**
     * 班次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 开始时间 HH:mm
     */
    private String startTime;

    /**
     * 结束时间 HH:mm（跨零点班次指次日）
     */
    private String endTime;

    /**
     * 跨零点标记（1-跨零点，结束时间属次日；0-同日起止）
     */
    private Integer crossDay;

    /**
     * 时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 适用科室ID（NULL=全院通用）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 班次类型（1-上午 2-下午 3-全天；NULL=不联动）
     */
    private Integer scheduleType;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 适用域（1-门诊排班 2-病区护理，ShiftUseScopeEnum）
     */
    private Integer useScope;

    /**
     * 适用岗位类别（StaffTypeEnum 码值，NULL=全部岗位通用）
     */
    private Integer applyStaffType;

    /**
     * 是否夜班（1-夜班 0-白班）
     */
    private Integer isNight;

    /**
     * 下此班后最短休息小时数（0=不限制）
     */
    private java.math.BigDecimal needRestHours;

    /**
     * 迟到宽限（分钟）：签到晚于「班次开始 + 这个数」才算迟到（sql/214）
     */
    private Integer lateGraceMinutes;

    /**
     * 备注
     */
    private String remark;
}
