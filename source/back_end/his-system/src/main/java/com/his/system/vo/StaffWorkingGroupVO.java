package com.his.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 在岗人次聚合行（BizStaffScheduleMapper#groupWorkingByUnitShift 的返回行）。
 */
@Data
public class StaffWorkingGroupVO implements Serializable {

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;

    /**
     * 排班单元类型（1-病区 2-科室 0-全院，见 OrgUnitTypeEnum）
     */
    private Integer orgType;

    /**
     * 排班单元ID（全院固定为 0）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /**
     * 排班单元名称快照
     */
    private String orgName;

    /**
     * 班次ID；0 表示全班次共用（人力缺口比对里按跨班次合计处理）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 岗位类型（见 StaffTypeEnum）
     */
    private Integer staffType;

    /**
     * 该组合下的在岗人次（COUNT(*)，同一人同日两班算两份）
     */
    private Long cnt;
}
