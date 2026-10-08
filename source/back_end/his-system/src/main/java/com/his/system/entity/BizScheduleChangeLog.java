package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 排班变更留痕（换班/代班/停班/加减号/出诊变更）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_schedule_change_log")
public class BizScheduleChangeLog extends BaseEntity {

    /**
     * 员工排班ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    /**
     * 变更类型（1-换班 2-代班 3-停班 4-加号 5-减号 6-出诊变更）
     */
    private Integer actionType;

    /**
     * 原值班人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromEmployeeId;

    /**
     * 实际值班人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toEmployeeId;

    /**
     * 原班次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromShiftId;

    /**
     * 新班次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toShiftId;

    /**
     * 变更数量
     */
    private Integer amount;

    /**
     * 变更原因
     */
    private String reason;

    /**
     * 变更时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;
}
