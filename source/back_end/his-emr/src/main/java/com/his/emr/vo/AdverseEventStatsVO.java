package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 不良事件工作台统计 VO（本月口径）
 */
@Data
public class AdverseEventStatsVO {

    /**
     * 本月上报数
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long monthReported;

    /**
     * 待处理数（状态=1，全量口径）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pending;

    /**
     * 本月 I 级警讯事件数
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sentinel;

    /**
     * 本月上报且已结案数
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long closed;
}
