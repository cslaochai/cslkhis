package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 总值班排班分页查询。
 *
 * <p>默认只查「今天起往后 30 天 + 昨天」：历史排班一年几百行，翻看过去没有意义，
 * 昨天的夜班必须带出来 —— 凌晨 0~8 点解析的就是它。
 */
@Data
public class DutyRosterQueryPageDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 起始日期（含）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /**
     * 截止日期（含）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 班次（1-白班 2-夜班 00-次日08）
     */
    private Integer shiftType;

    /**
     * 班内角色（1-主班 2-副班）
     */
    private Integer roleType;

    /**
     * 值班人
     */
    private Long employeeId;

    /**
     * 值班点位ID（按点位看某一天的排班）
     */
    private Long postId;

    /**
     * 责任范围（1-全院行政 2-急诊 3-感染 4-总务 5-信息 6-临床科室）
     */
    private Integer dutyScope;

    /**
     * 只看总值班行（true＝排除临床科室医师值班，只看全院行政那几条）。
     * 前端「今日排班」表默认传 true：那张表回答的是「此刻全院谁负责」，
     * 混进 24 个科室的一线二线三线就没人看得懂了。
     */
    private Boolean adminOnly;
}
