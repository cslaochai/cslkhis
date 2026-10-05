package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 总值班排班列表出参。
 *
 * <p>{@code actual*} 三列是「此刻真正该找的人」：有换班就指向换班后的人，
 * 没有就指向原值班人。列表与「当前总值班」解析必须用同一套取值，
 * 否则界面上写着张三、系统却把待办发给李四。
 */
@Data
public class DutyRosterVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 值班日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;

    /**
     * 值班点位ID（空＝这行是没点位的老数据）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long postId;

    /**
     * 点位名称
     */
    private String postName;

    /**
     * 责任范围（1-全院行政 2-急诊 3-感染 4-总务 5-信息 6-临床科室）
     */
    private Integer dutyScope;

    /**
     * 责任范围文本
     */
    private String dutyScopeText;

    /**
     * 值班层级（0-不适用 1-一线 2-二线 3-三线）
     */
    private Integer dutyLevel;

    /**
     * 值班层级文本
     */
    private String dutyLevelText;

    /**
     * 响应形态（1-坐班 2-听班 3-留院值班）
     */
    private Integer attendMode;

    /**
     * 响应形态文本
     */
    private String attendModeText;

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    private Integer orgType;

    /**
     * 排班单元ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /**
     * 班次（1-白班 2-夜班 00-次日08）
     */
    private Integer shiftType;

    /**
     * 班次文本（字典 his_duty_shift）
     */
    private String shiftTypeText;

    /**
     * 班内角色（1-主班 2-副班）
     */
    private Integer roleType;

    /**
     * 班内角色文本（字典 his_duty_role）
     */
    private String roleTypeText;

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
     * 值班人原属科室名称（快照）
     */
    private String deptName;

    /**
     * 值班联系电话
     */
    private String phone;

    /**
     * 班次开始时间（HH:mm）
     */
    private String startTime;

    /**
     * 班次结束时间（HH:mm）
     */
    private String endTime;

    /**
     * 状态（1-有效 0-停用）
     */
    private Integer status;

    /**
     * 是否已被临时换班
     */
    private Integer substituted;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long actualEmpId;

    private String actualEmpName;

    /**
     * 实际联系电话（换班后取换班电话 → 行内电话 → 员工档案手机）
     */
    private String actualPhone;

    /**
     * 换班后实际值班人姓名（快照）
     */
    private String substituteEmpName;

    /**
     * 换班时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime substituteTime;

    /**
     * 换班原因
     */
    private String substituteReason;

    /**
     * 备注
     */
    private String remark;
}
