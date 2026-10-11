package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 全院岗位排班（谁 · 哪天 · 在哪个单元 · 什么班 · 出不出勤）—— 排班事实的唯一载体。
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
     * 排班日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /**
     * 星期（1-周一 7-周日）
     */
    private Integer weekDay;

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
     * 科室ID（全院级为 0）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 排班对象（员工）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 工号
     */
    private String empCode;

    /**
     * 姓名
     */
    private String employeeName;

    /**
     * 所依据的岗位（人 × 科室 × 角色）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeePostId;

    /**
     * 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）
     */
    private Integer staffType;

    /**
     * 标准班次ID（0-无班次）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 开始时间 HH:mm（班次快照）
     */
    private String startTime;

    /**
     * 结束时间 HH:mm（班次快照，早于开始时间属次日）
     */
    private String endTime;

    /**
     * 出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班）
     */
    private Integer dutyStatus;

    /**
     * 响应形态（1-坐班 2-听班 3-留院值班）
     */
    private Integer attendMode;

    /**
     * 是否出诊（0-否 1-是）
     */
    private Integer clinicFlag;

    /**
     * 工时（分钟）
     */
    private Integer workMinutes;

    /**
     * 生成来源（1-手工 2-模板 3-复制周期 4-换班）
     */
    private Integer scheduleSource;

    /**
     * 来源排班周模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;
}
