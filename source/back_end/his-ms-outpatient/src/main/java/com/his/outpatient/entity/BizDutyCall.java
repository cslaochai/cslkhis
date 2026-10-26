package com.his.outpatient.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 临床值班听班呼叫单（sql/297）。
 *
 * <p>呼叫单是<b>运行时事件</b>，不是排班事实 —— 「谁在班」在 biz_schedule，
 * 「谁被叫过、多久没来」在这里。两者混一张表会把排班表搅成流水账。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_duty_call")
public class BizDutyCall extends BaseEntity {

    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;

    /** 呼叫单号 */
    private String callNo;

    /** 值班日期（夜班以开始日为准） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;

    /** 班次：1-白班 2-夜班（字典 his_duty_shift） */
    private Integer shiftType;

    /** 呼叫发起科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 呼叫发起科室名称（快照） */
    private String deptName;

    /** 被叫层级：2-二线 3-三线（DutyLevelEnum） */
    private Integer targetLevel;

    /** 被叫值班点位ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetPostId;

    /** 被叫值班点位名称（快照） */
    private String targetPostName;

    /** 被叫值班人 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetEmpId;

    /** 被叫值班人姓名（快照） */
    private String targetEmpName;

    /** 被叫值班联系电话 */
    private String targetPhone;

    /** 呼叫事由 */
    private String callReason;

    /** 患者姓名 */
    private String patientName;

    /** 患者所在位置（病区/床号） */
    private String patientLocation;

    /** 呼叫时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callTime;

    /** 到岗时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arriveTime;

    /** 状态：0-待到岗 1-已到岗 2-已超时升级 3-已取消（DutyCallStatusEnum） */
    private Integer status;

    /** 升级状态：0-未升级 1-已升级 */
    private Integer escalateStatus;

    /** 升级时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime escalateTime;

    /** 升级前的层级（2-二线 3-三线） */
    private Integer escalateFromLevel;

    /** 取消事由 */
    private String cancelReason;
}
