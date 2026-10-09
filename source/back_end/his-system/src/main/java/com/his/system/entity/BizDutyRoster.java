package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 全院总值班排班（一天 × 班次 × 主/副班）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_duty_roster")
public class BizDutyRoster extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 值班日期（夜班以开始日为准）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;

    /**
     * 班次：1-白班 2-夜班（DutyShiftTypeEnum，字典 his_duty_shift）
     */
    private Integer shiftType;

    /**
     * 班内角色：1-主班 2-副班（DutyRoleTypeEnum，主班查无/催不动时顶上）
     */
    private Integer roleType;

    /**
     * 标准班次ID（起止时刻与工时由它带出）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 值守点位ID（空=历史行，点位未定义）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long postId;

    /**
     * 员工排班ID（这个人这一班的在岗事实，换班后指向实际值班人）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    /**
     * 值班人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 值班人姓名
     */
    private String employeeName;

    /**
     * 值班人原属科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 原属科室名称（快照；总值班在班期间不代表该科室）
     */
    private String deptName;

    /**
     * 值班联系电话（空则回落员工档案手机）
     */
    private String phone;

    /**
     * 班次开始时间（HH:mm）
     */
    private String startTime;

    /**
     * 班次结束 HH:mm（夜班的 08:00 指次日）
     */
    private String endTime;

    /**
     * 1-有效 0-停用（停用不参与「当前总值班」解析）
     */
    private Integer status;

    /**
     * 临时换班后的实际值班人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long substituteEmpId;

    /**
     * 换班后实际值班人姓名
     */
    private String substituteEmpName;

    /**
     * 换班时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime substituteTime;

    /**
     * 换班原因（必填）
     */
    private String substituteReason;
}
