package com.his.emergency.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 急诊统计出参
 */
@Data
public class EmergencyStatsVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 今日急诊总数
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long todayTotal;

    /**
     * 候诊人数
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long waiting;

    /**
     * 就诊中人数
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long treating;

    /**
     * 留观人数
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long observation;

    /**
     * 红区人数
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long redZone;

    /**
     * 绿色通道人数
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long greenChannel;

    /**
     * 超时未接诊人数（候诊中且已超过登记时快照的应接诊时限）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long overdueWaiting;

    /**
     * 待派单池人数（候诊中且没有接诊医生）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long unassignedWaiting;

    /**
     * 留观超预警档人数（默认 &ge;48 小时：该开始张罗去向）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long obsOverWarn;

    /**
     * 留观超上限档人数（默认 &ge;72 小时：必须定去向，并会发待办催办）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long obsOverMax;

    /**
     * 留观预警阈值（小时，取自系统参数）。
     * 一并返回是为了让看板自己标出「留观超 48 小时」，而不是前端写死一个数字——
     * 阈值改了以后前端写死的档位就会和统计口径对不上。
     */
    private Integer obsWarnHours;

    /**
     * 留观上限阈值（小时，取自系统参数）
     */
    private Integer obsMaxHours;
}
