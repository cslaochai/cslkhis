package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 排班信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_schedule")
public class BizSchedule extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


    /**
     * 出勤事实ID（这条出诊计划是从哪条「谁 · 哪天 · 什么班」的排班事实派生出来的）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;

    /**
     * 星期（1-周一 2-周二 3-周三 4-周四 5-周五 6-周六 7-周日）
     */
    private Integer weekDay;

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
     * 诊室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    /**
     * 诊室名称
     */
    private String roomName;

    /**
     * 排班人员ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 排班人员姓名（同 {@link #doctorId} 的泛化说明）
     */
    private String doctorName;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他
     */
    private Integer staffType;

    /**
     * 开始时间（如08:00）
     */
    private String startTime;

    /**
     * 结束时间（如12:00）
     */
    private String endTime;

    /**
     * 班次ID（班次字典的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 总号源数
     */
    private Integer totalSource;

    /**
     * 已挂号数
     */
    private Integer usedSource;

    /**
     * 剩余号源数
     */
    private Integer availableSource;

    /**
     * 预约池已用号源（预约渠道扣减，退号按渠道还池）
     */
    private Integer usedAppointmentSource;

    /**
     * 挂号费
     */
    private BigDecimal registFee;

    /**
     * 诊查费
     */
    private BigDecimal diagnosisFee;

    /**
     * 是否专家号（0-否 1-是）
     */
    private Integer isExpert;

    /**
     * 专家号费用
     */
    private BigDecimal expertFee;

    /**
     * 是否可预约（0-否 1-是）
     */
    private Integer isAppointment;

    /**
     * 预约号源数
     */
    private Integer appointmentSource;

    /**
     * 累计加号数（加号同时加 total/available，本列留痕审计）
     */
    private Integer addedSource;

    /**
     * 状态（0-停诊 1-正常 2-已满 3-已过期）
     */
    private Integer status;

    /**
     * 就诊状态（0-待开始 1-接诊中 2-暂停）
     */
    private Integer consultStatus;
}
