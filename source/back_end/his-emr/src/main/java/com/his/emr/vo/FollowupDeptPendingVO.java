package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 随访看板：科室待办（未完成口径 = 待随访 + 随访中）。
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