package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 「此刻全院谁负责」——总值班解析结果。
 *
 * <p>这是整个模块的对外契约：{@code employeeId} 就是三条链路（急诊升级 / 床位跨科调配 /
 * 双向转诊）兜底收口人的员工ID，直接拿去当消息通知的 receiver_id。
 *
 * <p><b>查无此人时返回 null 而不是造一个假的人</b>：调用方拿到 null 才知道
 * 「今天全院没人负责」，前端据此显示红色告警条；一旦兜个默认值出来，
 * 界面永远是绿的，等于把"排班漏了"这个真问题藏起来。
 */
@Data
public class DutyOfficerVO {

    /**
     * 是否解析到总值班（false = 今天全院没人负责，前端必须显红告警）
     */
    private Integer found;

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
     * 联系电话（换班电话 → 行内电话 → 员工档案手机；都为空则 null）
     */
    private String phone;

    /**
     * 值班人原属科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 值班人原属科室名称
     */
    private String deptName;

    /**
     * 值班日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;

    /**
     * 班次（1-白班 2-夜班 00-次日08）
     */
    private Integer shiftType;

    private String shiftTypeText;

    /**
     * 班内角色（1-主班 2-副班）
     */
    private Integer roleType;

    private String roleTypeText;

    /**
     * 班次开始时间（HH:mm）
     */
    private String startTime;

    /**
     * 班次结束时间（HH:mm）
     */
    private String endTime;

    /**
     * 1 = 该班次原本排的人已被临时换班顶替
     */
    private Integer substituted;

    /**
     * 原排班人姓名（换班时才有；追责用）
     */
    private String originEmpName;

    /**
     * 查无总值班时的说明（前端直接展示）
     */
    private String emptyReason;
}
