package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 随访看板：科室待办（未完成口径 = 待随访 + 随访中）。
 *
 * <p>科室名以科室表为准、任务快照兜底：只信快照名的话，dept_id 指向已撤科室
 * 或快照没回填的行会和「根本没科室」挤成同一个「未指定科室」，两个榜并列在屏幕上
 * 却互不相干 —— 前者要人去补科室，后者是历史遗留，看板必须分得开。
 */
@Data
public class FollowupDeptPendingVO implements Serializable {

    /**
     * 科室ID（无科室时 SQL 给 0）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名（含「未指定科室」「科室#id」两种兜底文案）
     */
    private String deptName;

    /**
     * 未完成条数
     */
    private Long pendingCount;

    /**
     * 已完成条数
     */
    private Long doneCount;
}