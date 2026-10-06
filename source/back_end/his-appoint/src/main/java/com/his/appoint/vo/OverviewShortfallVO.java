package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 排班总览：人力缺口（实际在岗低于人力配置标准的组合，只报告不拦截）。
 */
@Data
public class OverviewShortfallVO {

    /**
     * 排班日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    private Integer orgType;

    /**
     * 排班单元ID（全院级为 0）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /**
     * 排班单元名称
     */
    private String orgName;

    /**
     * 班次名称（0-全部班次）
     */
    private String shiftName;

    /**
     * 岗位类别名称
     */
    private String staffTypeName;

    /**
     * 最低在岗人数
     */
    private Integer minStaff;

    /**
     * 实际在岗人次
     */
    private Long actualCount;

    /**
     * 缺口人数
     */
    private Integer shortfall;
}
