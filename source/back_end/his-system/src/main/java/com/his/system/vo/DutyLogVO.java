package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 值班日志列表/详情出参。
 *
 * <p>{@code canAck} 由后端算好给前端：接班人只在这一条的交班对象是自己、且还没签收时才能签收，
 * 这个判断放在前端做，迟早会和后端的不一致（AGENTS.md：接口字段名与前端读取名对不上 → 面板恒空且不报错）。
 */
@Data
public class DutyLogVO {

    /** 主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 值班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;

    /** 班次 1-白班 2-夜班（1-白班 2-夜班） */
    private Integer shiftType;

    /** 班次文案（白班/夜班） */
    private String shiftTypeText;

    /** 所属排班行全院总值班排班的ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rosterId;

    /** 值班人 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /** 值班人姓名（快照） */
    private String employeeName;

    /** 记录类型 1-值班事件 2-遗留事项 3-巡查记录（1-值班事件 2-遗留事项 3-巡查记录） */
    private Integer logType;

    /** 记录类型文案（值班事件/遗留事项/巡查记录） */
    private String logTypeText;

    /** 事件发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime happenTime;

    /** 标题 */
    private String title;

    /** 事件经过 */
    private String content;

    /** 处理情况 */
    private String handleResult;

    /** 状态 0-待处理 1-已处理 2-已交班（0-待处理 1-已处理 2-已交班） */
    private Integer status;

    /** 状态文案（待处理/已处理/已交班/已签收） */
    private String statusText;

    /** 接班人 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long handoverEmpId;

    /** 接班人姓名（快照） */
    private String handoverEmpName;

    /** 交班时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handoverTime;

    /** 接班人签收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ackTime;

    /** 记录人（代记/补记时 ≠ 值班人） */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 当前登录人是否可以签收这一条（只有交班对象本人能签收） */
    private Integer canAck;

    /** 备注 */
    private String remark;
}
