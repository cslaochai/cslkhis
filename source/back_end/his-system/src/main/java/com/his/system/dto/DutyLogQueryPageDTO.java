package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 值班日志分页查询。
 *
 * <p>默认「近 7 天」而不是全部：交班本是流水账，翻三个月前的记录没有意义，
 * 而默认带出当天和前一天是必须的 —— 接班人一进页面就要看到昨夜留了什么。
 */
@Data
public class DutyLogQueryPageDTO {

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 起始日期（含，默认 7 天前） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /** 截止日期（含，默认今天） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 班次 1-白班 2-夜班（1-白班 2-夜班） */
    private Integer shiftType;

    /** 记录类型 1-值班事件 2-遗留事项 3-巡查记录（1-值班事件 2-遗留事项 3-巡查记录） */
    private Integer logType;

    /** 状态 0-待处理 1-已处理 2-已交班（0-待处理 1-已处理 2-已交班） */
    private Integer status;

    /** 值班人 */
    private Long employeeId;

    /** 接班人（查"交给我的"用） */
    private Long handoverEmpId;
}
