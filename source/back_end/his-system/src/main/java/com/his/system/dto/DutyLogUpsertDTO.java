package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 值班日志新增/修改入参。
 *
 * <p>{@code employeeId} 留空 = 「记在我自己头上」：取当前时刻的总值班。
 * 代记/补记时才显式传别人 —— 于是"谁值班"和"谁写的"分成两列（{@code employeeId} / {@code createBy}）。
 *
 * <p><b>不接受直接传 status=2/3</b>：已交班、已签收只能由交班/签收动作推进，
 * 让登记接口能直接写"已签收"，等于可以自己给自己签字交接，交班本就成了摆设。
 */
@Data
public class DutyLogUpsertDTO {

    /** 主键 */
    private Long id;

    /** 值班日期（必填；夜班填开始日） */
    @NotNull(message = "值班日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;

    /** 班次 1-白班 2-夜班（1-白班 2-夜班） */
    @NotNull(message = "班次不能为空")
    private Integer shiftType;

    /** 值班人（留空 = 当前时刻的总值班） */
    private Long employeeId;

    /** 记录类型 1-值班事件 2-遗留事项 3-巡查记录（1-值班事件 2-遗留事项 3-巡查记录） */
    @NotNull(message = "记录类型不能为空")
    private Integer logType;

    /** 发生时间（留空 = 当前时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime happenTime;

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不超过 200 字")
    private String title;

    /** 事件经过 */
    @Size(max = 2000, message = "事件经过不超过 2000 字")
    private String content;

    /** 处理情况 */
    @Size(max = 1000, message = "处理情况不超过 1000 字")
    private String handleResult;

    /** 状态 0-待处理 1-已处理 2-已交班（0-待处理 1-已处理 2-已交班） */
    private Integer status;

    /** 备注 */
    private String remark;
}
