package com.his.outpatient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 听班呼叫单视图（sql/297）。
 */
@Data
public class DutyCallVO {

    /** 呼叫单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 呼叫单号 */
    private String callNo;

    /** 值班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;

    /** 班次：1-白班 2-夜班 */
    private Integer shiftType;

    /** 班次文案 */
    private String shiftTypeText;

    /** 呼叫发起科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 呼叫发起科室名称 */
    private String deptName;

    /** 被叫层级：2-二线 3-三线 */
    private Integer targetLevel;

    /** 被叫层级文案 */
    private String targetLevelText;

    /** 被叫值班点位ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetPostId;

    /** 被叫值班点位名称 */
    private String targetPostName;

    /** 被叫值班人 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetEmpId;

    /** 被叫值班人姓名 */
    private String targetEmpName;

    /** 被叫值班联系电话 */
    private String targetPhone;

    /** 呼叫事由 */
    private String callReason;

    /** 患者姓名 */
    private String patientName;

    /** 患者所在位置 */
    private String patientLocation;

    /** 呼叫时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callTime;

    /** 到岗时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arriveTime;

    /** 状态：0-待到岗 1-已到岗 2-已超时升级 3-已取消 */
    private Integer status;

    /** 状态文案 */
    private String statusText;

    /** 升级状态：0-未升级 1-已升级 */
    private Integer escalateStatus;

    /** 升级时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime escalateTime;

    /** 升级前的层级 */
    private Integer escalateFromLevel;

    /** 升级前的层级文案 */
    private String escalateFromLevelText;

    /** 取消事由 */
    private String cancelReason;

    /** 到岗时限（分钟） */
    private Integer timeoutMinutes;

    /** 到岗时限截止时间（由呼叫时间 + 时限算出，页面据此显示倒计时） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadlineTime;

    /** 距到岗时限剩余秒数（负数=已超时；已到岗/已取消为 null） */
    private Long remainSeconds;

    /** 当前登录人是否可应答到岗（只有被叫人本人可应答） */
    private Boolean canArrive;

    /** 当前登录人是否可取消（只有发起人可取消） */
    private Boolean canCancel;

    /** 呼叫流水（详情弹框用） */
    private List<DutyCallLogVO> logs;
}
