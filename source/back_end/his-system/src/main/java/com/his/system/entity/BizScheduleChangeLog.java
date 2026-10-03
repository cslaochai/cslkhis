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
 *
 * <p>取代原先两种土办法：值班侧在换班字段里存「原定谁 + 实际谁」，门诊侧把加减号写进备注字符串。
 * 两种都回答不了「这个人这个月换过几次班」，也追不了「这个号是谁加出来的」。
 *
 * <p><b>只写不改</b>：变更事实是审计凭据，一行一经写入代表发生过一件事，
 * 修改它等于销毁历史。要撤销就再写一条反向记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_schedule_change_log")
public class BizScheduleChangeLog extends BaseEntity {

    /** 员工排班ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    /** 变更类型（1-换班 2-代班 3-停班 4-加号 5-减号 6-出诊变更） */
    private Integer actionType;

    /** 原值班人 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromEmployeeId;

    /** 实际值班人 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toEmployeeId;

    /** 原班次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromShiftId;

    /** 新班次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toShiftId;

    /** 变更数量 */
    private Integer amount;

    /** 变更原因 */
    private String reason;

    /** 变更时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;
}
