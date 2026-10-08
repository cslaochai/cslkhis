package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 在岗人员 VO（排班的下游产出）。
 */
@Data
public class OnDutyStaffVO {

    /**
     * 排班ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 排班人员ID（排班信息的医师ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffId;

    private String staffName;

    /**
     * 排班对象岗位类别（2-护理 3-医技 4-药学 5-收费 6-行政其他）
     */
    private Integer staffType;

    /**
     * 岗位类别名（枚举带出，前端不用再存一份映射）
     */
    private String staffTypeName;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 标准班次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    private String shiftName;

    /**
     * 班次起止 HH:mm（来自排班快照，不查班次表）
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 只有医生出诊排班才有诊室，出勤岗位恒为 NULL
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    /**
     * 诊室名称
     */
    private String roomName;

    /**
     * 排班状态（0停用 1正常）
     */
    private Integer status;

    /**
     * 按查询时刻判定：此刻是否正在这一班里
     */
    private Boolean onDutyNow;
}
