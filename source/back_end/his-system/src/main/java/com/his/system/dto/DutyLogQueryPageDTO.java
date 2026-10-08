package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 值班日志分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DutyLogQueryPageDTO extends PageParam {

    /**
     * 起始日期（含，默认 7 天前）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /**
     * 截止日期（含，默认今天）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 班次 1-白班 2-夜班（1-白班 2-夜班）
     */
    private Integer shiftType;

    /**
     * 记录类型 1-值班事件 2-遗留事项 3-巡查记录（1-值班事件 2-遗留事项 3-巡查记录）
     */
    private Integer logType;

    /**
     * 状态 0-待处理 1-已处理 2-已交班（0-待处理 1-已处理 2-已交班）
     */
    private Integer status;

    /**
     * 值班人
     */
    private Long employeeId;

    /**
     * 接班人（查"交给我的"用）
     */
    private Long handoverEmpId;
}
